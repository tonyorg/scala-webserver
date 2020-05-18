package monarchy.graphql

import java.time.Instant

import monarchy.auth.AuthTooling
import monarchy.dal
import monarchy.dalwrite.WriteQueryBuilder
import sangria.schema._
import scala.util.{Try, Success, Failure};

case class WebResponse[T](
  success: Boolean,
  message: Option[String],
  data: Option[T] = None
)

case class GenerateTokenResponse(
  userId: Option[Long],
  bearerToken: Option[String]
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

      Field("generateToken", generateTokenType,
        resolve = { node =>
          import node.ctx.executionContext
          val user = dal.User(username = None, phoneNumber = None, secret = AuthTooling.generateSecret)
          node.ctx.queryCli.write(WriteQueryBuilder.put(user)).map { user =>
            val bearerToken = AuthTooling.generateSignature(user.id, user.secret)
            WebResponse(true, Option("Success"), Option(GenerateTokenResponse(Option(user.id), Option(bearerToken))))
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
              WebResponse(false, Option("Incorrect signature"))
            } else {
              val event = dal.Event(userId = args.id, domain = args.domain, path = args.path, startTime = Instant.ofEpochMilli(args.startTime), endTime = Instant.ofEpochMilli(args.endTime))
              Try(node.ctx.queryCli.write(WriteQueryBuilder.put(event))) match{
                case Success(result) =>
                  WebResponse(true, Option("Success"))
                case Failure(result) =>
                  WebResponse(false, Option("Failed"))
              }
            }
          }
        }
      )

    )
  )

  def genericResponseType = ObjectType(
    "registerResponse",
    fields[GraphqlContext, WebResponse[_]](
      Field("success", BooleanType,
        resolve = _.value.success
      ),
      Field("message", OptionType(StringType),
        resolve = _.value.message
      )
    )
  )

  def generateTokenType = ObjectType(
    "generateToken",
    fields[GraphqlContext, WebResponse[GenerateTokenResponse]](
      Field("success", BooleanType,
        resolve = _.value.success
      ),
      Field("message", OptionType(StringType),
        resolve = _.value.message
      ),
      Field("userId", OptionType(LongType),
        resolve = _.value.data.flatMap(_.userId)
      ),
      Field("bearerToken", OptionType(StringType),
        resolve = _.value.data.flatMap(_.bearerToken)
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
