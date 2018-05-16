package mb.pie.api

import java.io.Serializable

/**
 * Type for task inputs. Must be [Serializable], may NOT be `null`.
 */
typealias In = Serializable


/**
 * Executable task, consisting of a [TaskDef] and an input.
 */
data class Task<out I : In, out O : Out>(val id: String, val input: I) : Serializable {
  companion object {
    operator fun <I : In, O : Out, F : TaskDef<I, O>> invoke(@Suppress("UNUSED_PARAMETER") clazz: Class<F>, id: String, input: I): Task<I, O> {
      return Task<I, O>(id, input)
    }
  }

  constructor(taskDef: TaskDef<I, O>, input: I) : this(taskDef.id, input)


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(javaClass != other?.javaClass) return false
    other as Task<*, *>
    if(id != other.id) return false
    if(input != other.input) return false
    return true
  }

  private val hashCode: Int = id.hashCode() + 31 * input.hashCode()
  override fun hashCode(): Int {
    return hashCode
  }

  override fun toString() = "$id($input)"
  fun toShortString(maxLength: Int) = "$id(${input.toString().toShortString(maxLength)})"
}

/**
 * Untyped executable task.
 */
typealias UTask = Task<*, *>

/**
 * Generically typed executable task.
 */
typealias GTask = Task<In, Out>
