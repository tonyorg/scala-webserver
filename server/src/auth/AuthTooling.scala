package monarchy.auth

import java.security.SecureRandom
import java.util.Base64

import io.jsonwebtoken.impl.crypto.MacProvider
import io.jsonwebtoken.{Jwts, SignatureAlgorithm}
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object AuthTooling {
  def generateSecret: String = {
    Base64.getEncoder().encodeToString(MacProvider.generateKey.getEncoded)
  }

  def generateSignature(userId: Long, secret: String): String = {
    Jwts.builder.setSubject(userId.toString).signWith(SignatureAlgorithm.HS512, secret).compact
  }
//TODO: all the username/password logic
  def onInvalidUsernameErrorMessage: String = {
    "Incorrect username format. Username must contain " + "more than 1 character"
  }

  def isValidUsernameFormat(username: String): Boolean = {
    username.length > 0
  }

  def onInvalidPasswordErrorMessage: String = {
    "Incorrect password format. Password must contain " + "more than 1 character"
  }

  def isValidPasswordFormat(password: String): Boolean = {
    password.length > 0
  }

  def hashPassword(password: String, saltOpt: Option[String]): (String, String) = {
    val numIterations = 1000
    val salt = saltOpt match {
      case None =>
        val random = new SecureRandom()
        val out = new Array[Byte](3)
        random.nextBytes(out)
        out
      case Some(out) =>
        out.getBytes
    }

    val spec = new PBEKeySpec(password.toCharArray, salt, numIterations, 128)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = factory.generateSecret(spec).getEncoded
    (new String(hash), new String(salt))
  }

}
