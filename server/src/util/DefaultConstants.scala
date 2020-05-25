package monarchy.util

object DefaultConstantsTool{
  val fetchTopDomainsEntriesLimitMax: Int = 10;
  val fetchTopDomainsEntriesLimitDefault: Int = 7;
  def fetchTopDomainsGetMaxEntries(requestedLimitOpt: Option[Int]): Int = requestedLimitOpt match {
    case Some(requestedLimit) =>
      Math.min(requestedLimit, fetchTopDomainsEntriesLimitMax)
    case None =>
      fetchTopDomainsEntriesLimitDefault
  }
}