package mb.ceres

data class BuildInfo<out I : In, out O : Out>(val result: BuildRes<I, O>, val reason: BuildReason?) {
  constructor(result: BuildRes<I, O>) : this(result, null)

  val wasRebuilt = reason != null
}

typealias UBuildInfo = BuildInfo<*, *>