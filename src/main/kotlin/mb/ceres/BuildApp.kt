package mb.ceres

import java.io.Serializable

data class BuildApp<out I : In, out O : Out>(val builderId: String, val input: I) : Serializable {
  constructor(builder: Builder<I, O>, input: I) : this(builder.id, input)

  override fun toString() = "$builderId($input)"
  fun toShortString(maxLength: Int) = "$builderId(${input.toString().toShortString(maxLength)})"
}

typealias UBuildApp = BuildApp<*, *>
