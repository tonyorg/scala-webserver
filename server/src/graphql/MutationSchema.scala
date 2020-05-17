package monarchy.graphql

import java.time.Instant

import monarchy.auth.AuthTooling
import monarchy.dal
import monarchy.dalwrite.WriteQueryBuilder
import sangria.schema._


case class AuthResult(
  user: Option[dal.User],
  bearerToken: Option[String]
)

case class UserIdToken(
  userId: Option[Long],
  bearerToken: Option[String]
)

case class UpdateResponse(
  success: Boolean,
  message: Option[String]
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

      Field("createUser", userIdTokenType,
        resolve = { node =>
          import node.ctx.executionContext
          val user = dal.User(username = None, phoneNumber = None, secret = AuthTooling.generateSecret)
          node.ctx.queryCli.write(WriteQueryBuilder.put(user)).map { user =>
            val bearerToken = AuthTooling.generateSignature(user.id, user.secret)
            UserIdToken(Option(user.id), Option(bearerToken))
          }
        }
      ),

      Field("update", updateResponseType,
        arguments = List(Args.Update),
        resolve = { node =>
          import dal.PostgresProfile.Implicits._
          import node.ctx.executionContext
          val args = node.arg(Args.Update)
          val query = dal.User.query.filter(_.id === args.id)
          node.ctx.queryCli.first(query).map { user =>
            val bearerToken = user.map { u => AuthTooling.generateSignature(u.id, u.secret) }
            val token: String = bearerToken.getOrElse("")
            if (token != args.userSignature) {
              UpdateResponse(false, Option("Incorrect signature"))
            } else {
              val interval = dal.TabInterval(userId = args.id, url = args.url, startTime = Instant.ofEpochMilli(args.startTime), endTime = Instant.ofEpochMilli(args.endTime))
              node.ctx.queryCli.write(WriteQueryBuilder.put(interval))
              UpdateResponse(true, Option("Success"))
            }
          }
        }
      )

    )
  )

  def userIdTokenType = ObjectType(
    "userId",
    fields[GraphqlContext, UserIdToken](
      Field("userId", OptionType(LongType),
        resolve = _.value.userId
      ),
      Field("bearerToken", OptionType(StringType),
        resolve = _.value.bearerToken
      )
    )
  )

  def updateResponseType = ObjectType(
    "updateResponse",
    fields[GraphqlContext, UpdateResponse](
      Field("success", BooleanType,
        resolve = _.value.success
      ),
      Field("message", OptionType(StringType),
        resolve = _.value.message
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
