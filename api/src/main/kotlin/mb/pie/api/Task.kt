package mb.pie.api

import java.io.Serializable

/**
 * Executable task, consisting of a task identifier and an input.
 */
data class Task<I : In, out O : Out>(val taskDef: TaskDef<I, O>, val input: I) {
  fun key(): TaskKey {
    val key = taskDef.key(input)
    return TaskKey(taskDef.id, key)
  }

  override fun toString() = taskDef.desc(input)
}

/**
 * Untyped executable task.
 */
typealias UTask = Task<*, *>

/**
 * Generically typed executable task.
 */
typealias GTask = Task<In, Out>

/**
 * Key of an executable task, consisting of a task identifier and a key.
 */
data class TaskKey(val id: String, val key: Key) : Serializable {
  fun equals(other: TaskKey): Boolean {
    if(id != other.id) return false
    if(key != other.key) return false
    return true
  }

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(javaClass != other?.javaClass) return false
    return equals(other as TaskKey)
  }

  @Transient
  private val hashCode: Int = id.hashCode() + 31 * key.hashCode()

  override fun hashCode() = hashCode

  override fun toString() = "$id::Key($key)"
  fun toShortString(maxLength: Int) = "$id::Key(${key.toString().toShortString(maxLength)})"
}