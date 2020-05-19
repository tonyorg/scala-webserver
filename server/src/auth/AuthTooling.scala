package monarchy.auth

import java.util.Base64
import io.jsonwebtoken.impl.crypto.MacProvider
import io.jsonwebtoken.{Jwts, SignatureAlgorithm}

object AuthTooling {
  def generateSecret: String = {
    Base64.getEncoder().encodeToString(MacProvider.generateKey.getEncoded)
  }

  def generateSignature(userId: Long, secret: String): String = {
    Jwts.builder.setSubject(userId.toString).signWith(SignatureAlgorithm.HS512, secret).compact
  }

  def onInvalidUsernameErrorMessage: String = {
    "Incorrect username format. Username must contain " + "anything"
  }

  def isValidUsernameFormat(username: String): Boolean = {
    true//TODO: verify username (i.e. minlength, etc)
  }

  def onInvalidPasswordErrorMessage: String = {
    "Incorrect password format. Password must contain " + "anything"
  }

  def isValidPasswordFormat(password: String): Boolean = {
    true//TODO: verify password (i.e. minlength, numbers/characters, etc)
  }

  def hashPassword(password: String): String = {
    password //TODO: implement actual hashing
  }

}
