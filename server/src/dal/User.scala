package monarchy.dal

import java.time.Instant

case class User(
  id: Long = NewId,
  username: Option[String] = None,
  phoneNumber: Option[String] = None,
  rating: Int = 1000,
  secret: String,
  pwHash: Option[String] = None,
  salt: Option[String] = None,
  createdAt: Instant = Instant.now,
  updatedAt: Instant = Instant.now
)

import PostgresProfile.Implicits._
object User extends TableSchema(TableQuery[UserTable])

class UserTable(tag: Tag) extends TableDef[User](tag, "users") {
  def username = column[Option[String]]("username")
  def phoneNumber = column[Option[String]]("phone_number")
  def rating = column[Int]("rating")
  def secret = column[String]("secret")
  def pwHash = column[Option[String]]("pw_hash")
  def salt = column[Option[String]]("salt")
  def * = (id, username, phoneNumber, rating, secret, pwHash, salt, createdAt, updatedAt) <> ((User.apply _).tupled, User.unapply)
}
