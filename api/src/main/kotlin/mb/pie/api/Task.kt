package mb.pie.api

import java.io.Serializable

/**
 * Executable task, consisting of a task identifier and an input.
 */
data class Task<I : In, out O : Out>(private val taskDef: TaskDef<I, O>, val input: I) {
  val id = taskDef.id

  fun key(): TaskKey {
    val key = taskDef.key(input)
    return TaskKey(taskDef.id, key)
  }

  fun exec(ctx: ExecContext): O {
    return with(taskDef) { ctx.exec(input) }
  }

  @JvmOverloads
  fun desc(maxLength: Int = 100): String {
    return taskDef.desc(input, maxLength)
  }

  override fun toString() = desc()
}

/**
 * Untyped executable task.
 */
typealias UTask = Task<*, *>

/**
 * Key of an executable task, consisting of a task identifier and a key.
 */
data class TaskKey(val id: String, val key: Key) : Serializable {
  fun toTask(taskDefs: TaskDefs, txn: StoreReadTxn): Task<*, *> {
    val taskDef = taskDefs.getTaskDef<In, Out>(id)
      ?: throw RuntimeException("Cannot get task for task key $this; task definition with id $id does not exist")
    val input = txn.input(this)
      ?: throw RuntimeException("Cannot get task for task key $this; input object does not exist")
    return Task(taskDef, input)
  }


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

  @JvmOverloads
  fun toShortString(maxLength: Int = 100) = "#$id(${key.toString().toShortString(maxLength)})"

  override fun toString() = toShortString()
}
