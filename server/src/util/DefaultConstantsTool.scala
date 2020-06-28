package monarchy.util

import scala.concurrent.duration._

object DefaultConstantsTool{
  val fetchTopDomainsEntriesLimitMax: Int = 10;
  val fetchTopDomainsEntriesLimitDefault: Int = 7;
  val fetchTopDomainsTimeLimitMax: Long = Duration(30, DAYS).toMillis;
  val fetchTopDomainsTimeLimitDefault: Long = Duration(7, DAYS).toMillis;

  def fetchTopDomainsGetMaxEntries(requestedLimitOpt: Option[Int], overrideMax: Boolean = false): Int = requestedLimitOpt match {
    case Some(requestedLimit) =>
      if(overrideMax) requestedLimit
      else Math.min(requestedLimit, fetchTopDomainsEntriesLimitMax)
    case None =>
      fetchTopDomainsEntriesLimitDefault
  }

  def fetchTopDomainsGetTimeLimit(requestedLimitOpt: Option[String], overrideMax: Boolean = false): Long = requestedLimitOpt match {
    case Some(requestedLimit) =>
      val requestedDur = requestedLimit.toLong
      if (overrideMax) requestedDur
      else if (requestedDur < fetchTopDomainsTimeLimitDefault) requestedDur
      else fetchTopDomainsTimeLimitDefault
    case None =>
      fetchTopDomainsTimeLimitDefault
  }
}