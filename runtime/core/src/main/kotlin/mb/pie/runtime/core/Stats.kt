package mb.pie.runtime.core


object Stats {
  public var requires = 0
  public var executions = 0
  public var fileReqs = 0
  public var fileGens = 0
  public var callReqs = 0

  fun reset() {
    requires = 0
    executions = 0
    fileReqs = 0
    fileGens = 0
    callReqs = 0
  }

  fun addRequires() {
    ++requires
  }

  fun addExecution() {
    ++executions
  }

  fun addFileReq() {
    ++fileReqs
  }

  fun addFileGen() {
    ++fileGens
  }

  fun addCallReq() {
    ++callReqs
  }
}