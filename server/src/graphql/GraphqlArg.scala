package monarchy.graphql

import java.time.Instant

import monarchy.util.Json
import sangria.macros.derive.{deriveInputObjectType => deriveInput}
import sangria.marshalling.{FromInput, ResultMarshaller}
import sangria.schema._
import scala.reflect.runtime.universe.TypeTag

abstract class GraphqlArg[T: TypeTag] {
  implicit val fromInput: FromInput[T] = new FromInput[T] {
    val marshaller = ResultMarshaller.defaultResultMarshaller
    def fromResult(node: marshaller.Node): T = {
      Json.parse[T](Json.stringify(node))
    }
  }
}

case class RegisterQuery(id: Option[String], bearerToken: Option[String], username: String, password: String)
object RegisterQuery extends GraphqlArg[RegisterQuery]

case class TrackEventQuery(id: Long, bearerToken: String, domain: String, path: String, startTime: Long, endTime: Long)
object TrackEventQuery extends GraphqlArg[TrackEventQuery]

case class AuthQuery(phoneNumber: String)
object AuthQuery extends GraphqlArg[AuthQuery]

case class GamesQuery(userId: String)
object GamesQuery extends GraphqlArg[GamesQuery]



object Args {
  // Argument types
  val Register = Argument("q", deriveInput[RegisterQuery](), description = "Attempt to add an username to an user")
  val TrackEvent = Argument("q", deriveInput[TrackEventQuery](), description = "Attempt to track an event then add it to user.")
  val Id = Argument("id", StringType, description = "ID of this entity.")
  val Auth = Argument("q", deriveInput[AuthQuery](), description = "Query to initiate auth request.")
  val Games = Argument("q", deriveInput[GamesQuery](), description = "Query for games matching the criteria.")
}


