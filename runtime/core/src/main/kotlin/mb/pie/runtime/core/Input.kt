package mb.pie.runtime.core

import java.io.Serializable


typealias In = Serializable?

data class FuncApp<out I : In, out O : Out>(val id: String, val input: I) : Serializable {
  companion object {
    operator fun <I : In, O : Out, F : Func<I, O>> invoke(@Suppress("UNUSED_PARAMETER") clazz: Class<F>, id: String, input: I): FuncApp<I, O> {
      return FuncApp<I, O>(id, input)
    }
  }

  constructor(func: Func<I, O>, input: I) : this(func.id, input)


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(javaClass != other?.javaClass) return false
    other as FuncApp<*, *>
    if(id != other.id) return false
    if(input != other.input) return false
    return true
  }

  val hashCode: Int = id.hashCode() + 31 * (input?.hashCode() ?: 0)
  override fun hashCode(): Int {
    return hashCode
  }

  override fun toString() = "$id($input)"
  fun toShortString(maxLength: Int) = "$id(${input.toString().toShortString(maxLength)})"
}

typealias UFuncApp = FuncApp<*, *>
