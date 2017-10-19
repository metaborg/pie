package mb.pie.runtime.core

import java.io.Serializable


typealias In = Serializable?

data class FuncApp<out I : In, out O : Out>(val builderId: String, val input: I) : Serializable {
  constructor(func: Func<I, O>, input: I) : this(func.id, input)

  override fun toString() = "$builderId($input)"
  fun toShortString(maxLength: Int) = "$builderId(${input.toString().toShortString(maxLength)})"
}

typealias UFuncApp = FuncApp<*, *>
