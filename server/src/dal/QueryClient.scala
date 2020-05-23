package monarchy.dal

import scala.concurrent.{ExecutionContext, Future}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.util.Try

trait QueryClient {
  import PostgresProfile.api._
  def all[E](query: Query[Table[E], E, Seq]): Future[Seq[E]]
  def first[E](query: Query[Table[E], E, Seq]): Future[Option[E]]
  def read[E](dbio: DBIO[E]): Future[E]
  def write[E](dbio: DBIO[E]): Future[E]
  def attemptWrite[E](dbio: DBIO[E]): Future[Try[E]]
  def getConnection: JdbcProfile#Backend#Database
}

case class QueryClientImpl(
  cfg: DatabaseConfig[JdbcProfile]
)(implicit ec: ExecutionContext) extends QueryClient {
  import PostgresProfile.api._

  private val connection = cfg.db

  override def getConnection = {
    connection
  }
  // Methods for reading.
  override def all[E](query: Query[Table[E], E, Seq]): Future[Seq[E]] = {
    connection.run(query.result)
  }

  override def first[E](query: Query[Table[E], E, Seq]): Future[Option[E]] = {
    all(query.take(1)).map(_.headOption)
  }

  override def read[E](dbio: DBIO[E]): Future[E] = {
    connection.run(dbio)
  }



  // Methods for writing.
  override def write[E](dbio: DBIO[E]): Future[E] = {
    connection.run(dbio.transactionally)
  }

  override def attemptWrite[E](dbio: DBIO[E]): Future[Try[E]] = {
    connection.run(dbio.asTry)
  }


}
