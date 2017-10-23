package mb.pie.runtime.core


data class ExecInfo<out I : In, out O : Out>(val result: ExecRes<I, O>, val reason: ExecReason?) {
  constructor(result: ExecRes<I, O>) : this(result, null)

  val wasExecuted = reason != null
}

typealias UExecInfo = ExecInfo<*, *>
