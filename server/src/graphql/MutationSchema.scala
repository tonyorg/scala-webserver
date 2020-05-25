package monarchy.graphql

import java.time.Instant

import monarchy.auth.AuthTooling
import monarchy.dal
import monarchy.dal.{Event, User}
import monarchy.dalwrite.WriteQueryBuilder
import sangria.schema._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try};

case class WebResponse[T](
  success: Boolean,
  message: Option[String],
  data: Option[T] = None
)

case class CredentialsResponse(
  userId: Option[String] = None,
  bearerToken: Option[String] = None,
  username: Option[String] = None
)

case class AuthResult(
  user: Option[dal.User],
  bearerToken: Option[String]
)

object MutationSchema {
  lazy val Def = ObjectType(
    "Mutation",
    fields[GraphqlContext, Unit](
      Field("auth", BooleanType,
        arguments = List(Args.Auth),
        resolve = { node =>
          val query = node.arg(Args.Auth)
          println(s"web-server >> initiating auth: $query")
          true
        }
      ),

      Field("generateToken", credentialsType,
        resolve = { node =>
          import node.ctx.executionContext
          val user = dal.User(username = None, phoneNumber = None, secret = AuthTooling.generateSecret)
          node.ctx.queryCli.write(WriteQueryBuilder.put(user)).map { user =>
            val bearerToken = AuthTooling.generateSignature(user.id, user.secret)
            WebResponse(success = true, Option("Success"), Option(CredentialsResponse(Option(user.id.toString), Option(bearerToken))))
          }
        }
      ),

      Field("trackEvent", genericResponseType,
        arguments = List(Args.TrackEvent),
        resolve = { node =>
          import dal.PostgresProfile.Implicits._
          import node.ctx.executionContext
          val args = node.arg(Args.TrackEvent)
          val query = dal.User.query.filter(_.id === args.id)
          node.ctx.queryCli.first(query).map { user =>

            val bearerToken = user.map { u => AuthTooling.generateSignature(u.id, u.secret) }
            val token: String = bearerToken.getOrElse("")
            if (token == "" || token != args.bearerToken) {
              WebResponse(success = false, Option("Incorrect signature"))
            } else {
              val event = dal.Event(userId = args.id, domain = args.domain, path = args.path, startTime = Instant.ofEpochMilli(args.startTime), endTime = Instant.ofEpochMilli(args.endTime))
              Try(node.ctx.queryCli.write(WriteQueryBuilder.put(event))) match{
                case Success(result) =>
                  WebResponse(success = true, Option("Success"))
                case Failure(result) =>
                  WebResponse(success = false, Option("Failed"))
              }
            }
          }
        }
      ),

      Field("registerUser", credentialsType,
        arguments = List(Args.Credentials),
        resolve = {node =>
          import dal.PostgresProfile.Implicits._
          import node.ctx.executionContext
          import org.postgresql.util.PSQLException
          val args = node.arg(Args.Credentials)
          if (!AuthTooling.isValidUsernameFormat(args.username)) {
            Future.successful(WebResponse(success = false, Option(AuthTooling.onInvalidUsernameErrorMessage), Option(CredentialsResponse())))
          } else if (!AuthTooling.isValidPasswordFormat(args.password)) {
            Future.successful(WebResponse(success = false, Option(AuthTooling.onInvalidPasswordErrorMessage), Option(CredentialsResponse())))
          } else {
            def createNewUser(username: String, password: String): User = {
              val hashed = AuthTooling.hashPassword(password, None)
              dal.User(username = Option(args.username), secret = AuthTooling.generateSecret, pwHash = Option(hashed._1), salt = Option(hashed._2))
            }
            val userReq: Future[(String, User)] = (args.id, args.bearerToken) match {
              case (Some(idStr), Some(actualToken)) =>
                val id = idStr.toLong
                val query = dal.User.query.filter(_.id === id);
                node.ctx.queryCli.first(query).map {
                  case Some(user) =>
                    val expectedToken = AuthTooling.generateSignature(user.id, user.secret)
                    if (actualToken != expectedToken) {
                      ("Invalid signature, new user created", createNewUser(args.username, args.password))
                    } else {
                      user.username match {
                        case Some(oldUsername) =>
                          ("Already registered with username: " + oldUsername, user)
                        case _ =>
                          val hashed = AuthTooling.hashPassword(args.password, None)
                          ("Successfully registered with username: " + args.username, user.copy(username = Option(args.username), pwHash = Option(hashed._1), salt = Option(hashed._2)))
                      }
                    }
                  case _ =>
                    ("ID not found, new user created", createNewUser(args.username, args.password))
                }
              case _ =>
                Future.successful("ID not found, new user created", createNewUser(args.username, args.password))
            }
            userReq.flatMap { req =>
              node.ctx.queryCli.attemptWrite(WriteQueryBuilder.put(req._2)).map {
                case Success(user) =>
                  val bearerToken = AuthTooling.generateSignature(user.id, user.secret)
                  WebResponse(success = true, Option(req._1), Option(CredentialsResponse(Option(user.id.toString), Option(bearerToken), Option(args.username))))
                case Failure(e: PSQLException) if(e.getSQLState == "23505") =>
                  WebResponse(success = false, Option("Username already exists"), Option(CredentialsResponse()))
                case Failure(_) =>
                  WebResponse(success = false, Option("Something went wrong adding user"), Option(CredentialsResponse()))
              }
            }
          }

        }
      )

    )
  )

  def genericResponseType = ObjectType(
    "genericResponse",
    fields[GraphqlContext, WebResponse[_]](
      Field("success", BooleanType,
        resolve = _.value.success
      ),
      Field("message", OptionType(StringType),
        resolve = _.value.message
      )
    )
  )

  def credentialsType = ObjectType(
    "credentials",
    fields[GraphqlContext, WebResponse[CredentialsResponse]](
      Field("success", BooleanType,
        resolve = _.value.success
      ),
      Field("message", OptionType(StringType),
        resolve = _.value.message
      ),
      Field("userId", OptionType(StringType),
        resolve = _.value.data.flatMap(_.userId)
      ),
      Field("bearerToken", OptionType(StringType),
        resolve = _.value.data.flatMap(_.bearerToken)
      ),
      Field("username", OptionType(StringType),
        resolve = _.value.data.flatMap(_.username)
      )
    )
  )


  def authType = ObjectType(
    "Auth",
    fields[GraphqlContext, AuthResult](
      Field("user", OptionType(QuerySchema.UserType),
        resolve = _.value.user
      ),
      Field("userId", OptionType(StringType),
        resolve = _.value.user.map(_.id.toString)
      ),
      Field("bearerToken", OptionType(StringType),
        resolve = _.value.bearerToken
      ),
      Field("loggedIn", BooleanType,
        resolve = _.value.bearerToken.nonEmpty
      )
    )
  )
}
