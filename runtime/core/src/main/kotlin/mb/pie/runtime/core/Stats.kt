package mb.pie.runtime.core

/**
 * HACK: global object for collecting build statistics.
 */
object Stats {
  var requires = 0
  var executions = 0
  var fileReqs = 0
  var fileGens = 0
  var callReqs = 0

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
