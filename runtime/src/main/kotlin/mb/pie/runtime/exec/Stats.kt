package mb.pie.runtime.exec

/**
 * HACK: global object for collecting build statistics.
 */
object Stats {
  public var requires: Int = 0;
  public var executions: Int = 0;
  public var fileReqs: Int = 0;
  public var fileGens: Int = 0;
  public var callReqs: Int = 0;

  public fun reset() {
    requires = 0;
    executions = 0;
    fileReqs = 0;
    fileGens = 0;
    callReqs = 0;
  }

  public fun addRequires() {
    ++requires;
  }

  public fun addExecution() {
    ++executions;
  }

  public fun addFileReq() {
    ++fileReqs;
  }

  public fun addFileGen() {
    ++fileGens;
  }

  public fun addCallReq() {
    ++callReqs;
  }
}
