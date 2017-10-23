package mb.pie.runtime.core

import java.io.Serializable


typealias In = Serializable?

data class FuncApp<out I : In, out O : Out>(val id: String, val input: I) : Serializable {
  constructor(func: Func<I, O>, input: I) : this(func.id, input)

  override fun toString() = "$id($input)"
  fun toShortString(maxLength: Int) = "$id(${input.toString().toShortString(maxLength)})"
}

typealias UFuncApp = FuncApp<*, *>
