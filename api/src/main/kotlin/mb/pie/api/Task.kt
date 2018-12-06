package mb.pie.api

import java.io.Serializable

/**
 * Executable task, consisting of a [task definition][TaskDef] and its [input].
 */
data class Task<I : In, O : Out>(private val taskDef: TaskDef<I, O>, val input: I) {
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


  fun toSTask(): STask<I> {
    return STask(taskDef.id, input)
  }


  fun equals(other: Task<I, O>): Boolean {
    if(taskDef.id != other.taskDef.id) return false
    if(input != other.input) return false
    return true
  }

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(javaClass != other?.javaClass) return false
    @Suppress("UNCHECKED_CAST")
    return equals(other as Task<I, O>)
  }

  override fun hashCode() = taskDef.id.hashCode() + 31 * input.hashCode()

  override fun toString() = desc()
}

/**
 * Serializable task, consisting of the [identifier of a task definition][id], and its [input].
 */
data class STask<I : In>(val id: String, val input: I) : Serializable {
  fun <O : Out> toTask(taskDefs: TaskDefs): Task<I, O> {
    val taskDef = taskDefs.getTaskDef<I, O>(id)
      ?: throw RuntimeException("Cannot get task definition for id $id; task definition with that id does not exist")
    return Task(taskDef, input)
  }


  @JvmOverloads
  fun toShortString(maxLength: Int = 100) = "$id(${input.toString().toShortString(maxLength)})"

  override fun toString() = toShortString()
}

/**
 * Key of a task, consisting of a [task definition identifier][id] and a [key].
 */
data class TaskKey(val id: String, val key: Key) : Serializable {
  fun toTask(taskDefs: TaskDefs, txn: StoreReadTxn): Task<*, *> {
    val taskDef = taskDefs.getTaskDef<In, Out>(id)
      ?: throw RuntimeException("Cannot get task definition for task key $this; task definition with id $id does not exist")
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
  private var hashCodeIsCached = false
  @Transient
  private var hashCodeCache: Int = 0

  override fun hashCode(): Int {
    if(!hashCodeIsCached) {
      hashCodeCache = id.hashCode() + 31 * key.hashCode()
      hashCodeIsCached = true
    }
    return hashCodeCache
  }

  @JvmOverloads
  fun toShortString(maxLength: Int = 100) = "#$id(${key.toString().toShortString(maxLength)})"

  override fun toString() = toShortString()
}
