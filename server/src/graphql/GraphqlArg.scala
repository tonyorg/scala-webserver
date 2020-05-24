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

case class CredentialsQuery(id: Option[String], bearerToken: Option[String], username: String, password: String)
object CredentialsQuery extends GraphqlArg[CredentialsQuery]

case class TopDomainsQuery(userId: String, bearerToken: String, numTop: Option[Int])
object TopDomainsQuery extends GraphqlArg[TopDomainsQuery]

case class TrackEventQuery(id: Long, bearerToken: String, domain: String, path: String, startTime: Long, endTime: Long)
object TrackEventQuery extends GraphqlArg[TrackEventQuery]

case class AuthQuery(phoneNumber: String)
object AuthQuery extends GraphqlArg[AuthQuery]

case class GamesQuery(userId: String)
object GamesQuery extends GraphqlArg[GamesQuery]



object Args {
  // Argument types
  val Credentials = Argument("q", deriveInput[CredentialsQuery](), description = "Client sent username and password credentials.")
  val TopDomains = Argument("q", deriveInput[TopDomainsQuery](), description = "Query to fetch top domains user has visited.")
  val TrackEvent = Argument("q", deriveInput[TrackEventQuery](), description = "Attempt to track an event then add it to user.")
  val Id = Argument("id", StringType, description = "ID of this entity.")
  val Auth = Argument("q", deriveInput[AuthQuery](), description = "Query to initiate auth request.")
  val Games = Argument("q", deriveInput[GamesQuery](), description = "Query for games matching the criteria.")
}


