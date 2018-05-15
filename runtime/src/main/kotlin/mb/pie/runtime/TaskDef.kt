package mb.pie.runtime


/**
 * Definition of an executable task.
 */
interface TaskDef<in I : In, out O : Out> {
  @Throws(ExecException::class, InterruptedException::class)
  fun ExecContext.exec(input: I): O

  fun desc(input: I): String = "$id(${input.toString().toShortString(100)})"

  val id: String get() = this::class.java.canonicalName!!
}

/**
 * Untyped [TaskDef].
 */
typealias UTaskDef = TaskDef<*, *>

/**
 * Generically typed [TaskDef].
 */
typealias GTaskDef = TaskDef<In, Out>

/**
 * [TaskDef] implementation using anonymous functions.
 */
open class LambdaTaskDef<in I : In, out O : Out>(
  override val id: String,
  private val execFunc: ExecContext.(I) -> O,
  private val descFunc: ((I) -> String)? = null
) : TaskDef<I, O> {
  override fun ExecContext.exec(input: I): O = execFunc(input)
  override fun desc(input: I): String = descFunc?.invoke(input) ?: "$id(${input.toString().toShortString(100)})"
}
