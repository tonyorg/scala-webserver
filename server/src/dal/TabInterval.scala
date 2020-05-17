package monarchy.dal

import java.time.Instant

case class TabInterval(
  id: Long = NewId,
  userId: Long,
  url: String,
  startTime: Instant,
  endTime: Instant,
  createdAt: Instant = Instant.now,
  updatedAt: Instant = Instant.now
)

import PostgresProfile.Implicits._
object TabInterval extends TableSchema(TableQuery[IntervalTable])

class IntervalTable(tag: Tag) extends TableDef[TabInterval](tag, "intervals") {
  def userId = column[Long]("user_id")
  def url = column[String]("url")
  def startTime = column[Instant]("start_time")
  def endTime = column[Instant]("end_time")
  def * = (id, userId, url, startTime, endTime, createdAt, updatedAt) <> ((TabInterval.apply _).tupled, TabInterval.unapply)
}
