package monarchy.graphql

import java.time.Duration

import monarchy.auth.AuthTooling
import monarchy.dal
import monarchy.dal.Event
import monarchy.game
import monarchy.marshalling.game.GameStringDeserializer
import monarchy.util.Json
import sangria.schema._

import scala.collection.immutable.HashMap
import scala.concurrent.{ExecutionContext, Future}

object QuerySchema {
  case class DomainResponse(
    domain: String,
    duration: Option[Duration]
  )

  lazy val DomainType = ObjectType(
    "Domain",
    fields[GraphqlContext, DomainResponse](
      Field("domain", StringType, resolve = _.value.domain),
      Field("duration", OptionType(LongType), resolve = _.value.duration match{
        case Some(duration) =>
          Option(duration.toMillis)
        case None =>
          None
      }),
    )
  )

  lazy val TopDomainsType = ObjectType(
    "TopDomains",
    fields[GraphqlContext, WebResponse[Seq[DomainResponse]]](
      Field("success", BooleanType,
        resolve = _.value.success
      ),
      Field("message", OptionType(StringType),
        resolve = _.value.message
      ),
      Field("events", OptionType(ListType(DomainType)),
        resolve = _.value.data
      )
    )
  )

  lazy val Def = ObjectType(
    "Query",

    fields[GraphqlContext, Unit](
      Field("user", OptionType(UserType),
        arguments = List(Args.Id),
        resolve = { node =>
          import dal.PostgresProfile.Implicits._
          val id = node.arg(Args.Id).toLong
          val query = dal.User.query.filter(_.id === id)
          node.ctx.queryCli.first(query)
        }
      ),

      Field("fetchTopDomains", TopDomainsType,
        arguments = List(Args.TopDomains),
        resolve = { node =>
          import dal.PostgresProfile.Implicits._
          import node.ctx.executionContext
          val args = node.arg(Args.TopDomains)
          val query = dal.User.query.filter(_.id === args.userId.toLong)
          val seqResp = node.ctx.queryCli.first(query).flatMap {
            case Some(user) =>
              val expectedToken = AuthTooling.generateSignature(user.id, user.secret)
              if (args.bearerToken != expectedToken) {
                Future(Seq.empty[DomainResponse])
              } else {
//                val query = dal.Event.query.filter(_.userId === args.userId.toLong).groupBy(event => (event.domain, event.path)).map{
//                  case ((domain, path), group) => (domain, path, group.map(event => event.endTime - event.startTime).sum)
//                }.sortBy(_._3.desc)
                val maxTop = 10;
                val defaultTop = 7;
                val topK = args.numTop match{
                  case Some(k) =>
                    Math.min(k, maxTop)
                  case None =>
                    defaultTop
                }
                val domainQuery = dal.Event.query.filter(_.userId === args.userId.toLong).groupBy(event => (event.domain)).map{
                  case (domain, group) => (domain, group.map(event => event.endTime - event.startTime).sum)
                }.sortBy(_._2.desc)
                val query = domainQuery.take(topK).unionAll(Query.apply(("Others", domainQuery.drop(topK).map(_._2).sum)))
                node.ctx.queryCli.getConnection.run(query.result.map(_.map{row =>
                  DomainResponse(domain = row._1, duration = row._2)}))
//                node.ctx.queryCli.all(query).map{eventSeq =>
//                  val domainHMap = scala.collection.mutable.HashMap[String, TopDomains]()
//                  eventSeq.foreach { event =>
//                    if (domainHMap.contains(event.domain)) {
//                      domainHMap(event.domain).duration += event.startTime.toEpochMilli - event.endTime.toEpochMilli;
//                    } else {
//                      domainHMap(event.domain) = TopDomains(duration = event.startTime.toEpochMilli - event.endTime.toEpochMilli, scala.collection.mutable.HashMap[String, Long])
//                    }
//                    if (domainHMap(event.domain).subPaths.contains(event.path)) {
//                      domainHMap(event.domain).subPaths(event.path) += event.startTime.toEpochMilli - event.endTime.toEpochMilli;
//                    } else {
//                      domainHMap(event.domain).subPaths(event.path) = event.startTime.toEpochMilli - event.endTime.toEpochMilli;
//                    }
//                    //if e >= 2 << n where n = top # to keep, e = number of elements, do O(n*e) instead of O(e log e)?
//                    domainHMap.
//                  }
//              }
              }
            case _ =>
              Future(Seq.empty[DomainResponse])
          }
          seqResp.map{seq =>
            if (seq.isEmpty) {
              WebResponse(false, Option("Invalid credentials"), Option(seq))
            } else {
              WebResponse(true, Option("Success"), Option(seq))
            }
          }
        }
      ),

//      Field("getAllEvents", EventsType,
//        arguments = List(Args.TokenId),
//        resolve = { node =>
//          import dal.PostgresProfile.Implicits._
//          import node.ctx.executionContext
//          val args = node.arg(Args.TokenId)
//          val query = dal.User.query.filter(_.id === args.userId.toLong)
//          val seqResp = node.ctx.queryCli.first(query).flatMap {
//            case Some(user) =>
//              val expectedToken = AuthTooling.generateSignature(user.id, user.secret)
//              if (args.bearerToken != expectedToken) {
//                Future(Seq.empty[Event])
//              } else {
//                val query = dal.Event.query.filter(_.userId === args.userId.toLong)
//                node.ctx.queryCli.all(query).map { eventSeq =>
//                  eventSeq.map(event => Event(id = event.id, userId = event.userId, domain = event.domain, path = event.path, startTime = event.startTime, endTime = event.endTime))
//                }
//              }
//            case _ =>
//              Future(Seq.empty[Event])
//          }
//          seqResp.map{seq =>
//            if (seq.isEmpty) {
//              WebResponse(false, Option("Invalid credentials"), Option(seq))
//            } else {
//              WebResponse(true, Option("Success"), Option(seq))
//            }
//          }
//        }
//      ),

      Field("game", OptionType(GameType),
        arguments = List(Args.Id),
        resolve = { node =>
          import dal.PostgresProfile.Implicits._
          val id = node.arg(Args.Id).toLong
          val query = dal.Game.query.filter(_.id === id)
          node.ctx.queryCli.first(query)
        }
      ),
      Field("games", ListType(GameType),
        arguments = List(Args.Games),
        resolve = { node =>
          import dal.PostgresProfile.Implicits._
          val userId = node.arg(Args.Games).userId.toLong
          val query = dal.Player.query
            .filter(_.userId === userId)
            .join(dal.Game.query).on(_.gameId === _.id)
            .map(_._2)
            .sortBy(_.id.desc)
          node.ctx.queryCli.all(query)
        }
      )
    )
  )


  lazy val PlayerType =
    ObjectType(
    "Player",
    fields[GraphqlContext, dal.Player](
      Field("status", StringType, resolve = _.value.status.toString),
      Field("user", OptionType(UserType), resolve = { node =>
        import dal.PostgresProfile.Implicits._
        val userId = node.value.userId
        val query = dal.User.query.filter(_.id === userId)
        node.ctx.queryCli.first(query)
      })
    )
  )

  lazy val UserType = ObjectType(
    "User",
    fields[GraphqlContext, dal.User](
      Field("id", StringType, resolve = _.value.id.toString),
      Field("rating", IntType, resolve = _.value.rating),
    )
  )
  lazy val GameType = ObjectType(
    "Game",
    fields[GraphqlContext, dal.Game](
      Field("id", StringType, resolve = _.value.id.toString),
      Field("status", StringType, resolve = _.value.status.toString),
      Field("players", ListType(PlayerType), resolve = { node =>
        import dal.PostgresProfile.Implicits._
        val gameId = node.value.id
        val query = dal.Player.query.filter(_.gameId === gameId)
        node.ctx.queryCli.all(query)
      }),
      Field("state", OptionType(GameStateType), resolve = { node =>
        import node.ctx.executionContext
        import GameStringDeserializer._
        val gameId = node.value.id
        node.ctx.redisCli.get[game.Game](s"monarchy/streaming/game/$gameId")
      })
    )
  )

  lazy val GameStateType = ObjectType(
    "GameState",
    fields[GraphqlContext, game.Game](
      Field("currentPlayerId", StringType, resolve = _.value.currentPlayer.id.id.toString),
      Field("currentSelection", OptionType(VecType), resolve = _.value.currentSelection),
      Field("tiles", ListType(TileType), resolve = _.value.board.tiles)
    )
  )

  lazy val TileType =  ObjectType(
    "Tile",
    fields[GraphqlContext, game.Tile](
      Field("point", VecType, resolve = _.value.point),
      Field("piece", OptionType(PieceType), resolve = _.value.piece)
    )
  )

  lazy val VecType = ObjectType(
    "Vec",
    fields[GraphqlContext, game.Vec](
      Field("i", IntType, resolve = _.value.i),
      Field("j", IntType, resolve = _.value.j)
    )
  )

  lazy val PieceType = ObjectType(
    "Piece",
    fields[GraphqlContext, game.Piece](
      Field("id", StringType, resolve = _.value.id.id.toString),
      Field("order", StringType, resolve = _.value.conf.toString),
      Field("name", StringType, resolve = _.value.conf.name),
      Field("playerId", StringType, resolve = _.value.playerId.id.toString),
      Field("currentHealth", IntType, resolve = _.value.currentHealth),
      Field("currentWait", IntType, resolve = _.value.currentWait),
      Field("currentDirection", VecType, resolve = _.value.currentDirection),
      Field("currentEffects", ListType(StringType), resolve = { node =>
        node.value.currentEffects.collect {
          case game.PieceEffect(_, e: game.Paralyze) => "Paralyzed"
        }
      }),
      Field("currentFocus", BooleanType, resolve = _.value.currentFocus),
      Field("currentBlocking", FloatType, resolve = { node =>
        node.value.conf.blocking + node.value.blockingAjustment
      })
    )
  )
}
