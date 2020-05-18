package monarchy.dal

import java.time.Instant

case class Event(
  id: Long = NewId,
  userId: Long,
  domain: String,
  path: String,
  startTime: Instant,
  endTime: Instant,
  createdAt: Instant = Instant.now,
  updatedAt: Instant = Instant.now
)

import PostgresProfile.Implicits._
object Event extends TableSchema(TableQuery[EventTable])

class EventTable(tag: Tag) extends TableDef[Event](tag, "events") {
  def userId = column[Long]("user_id")
  def domain = column[String]("domain")
  def path = column[String]("path")
  def startTime = column[Instant]("start_time")
  def endTime = column[Instant]("end_time")
  def * = (id, userId, domain, path, startTime, endTime, createdAt, updatedAt) <> ((Event.apply _).tupled, Event.unapply)
}
