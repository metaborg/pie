package mb.pie.runtime.core

import java.io.Serializable
import kotlin.reflect.KClass


typealias In = Serializable?

data class FuncApp<out I : In, out O : Out>(val id: String, val input: I) : Serializable {
  companion object {
    operator fun <I : In, O : Out, F : Func<I, O>> invoke(@Suppress("UNUSED_PARAMETER") clazz: Class<F>, id: String, input: I): FuncApp<I, O> {
      return FuncApp<I, O>(id, input)
    }
  }

  constructor(func: Func<I, O>, input: I) : this(func.id, input)

  override fun toString() = "$id($input)"
  fun toShortString(maxLength: Int) = "$id(${input.toString().toShortString(maxLength)})"
}

typealias UFuncApp = FuncApp<*, *>
