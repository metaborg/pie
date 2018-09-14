package mb.pie.api

import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.vfs.path.PPath

/**
 * Execution context used in execution of [tasks][Task], to require other tasks, and record dependencies to files.
 */
interface ExecContext {
  /**
   * Requires given task and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper? = null): O

  /**
   * Requires task given by its task definition and input, and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper? = null): O

  /**
   * Requires task given by its serializable task form, and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(task: STask<I>, stamper: OutputStamper? = null): Out

  /**
   * Requires task given by the identifier of its task definition and input, and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper? = null): Out

  /**
   * Requires task given by its key, and return its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun require(key: TaskKey, stamper: OutputStamper? = null): Out


  /**
   * Marks given file as required (read), creating a required file dependency.
   */
  fun require(file: PPath, stamper: FileStamper? = null)

  /**
   * Marks given file as generated (written to/created), creating a generated file dependency.
   */
  fun generate(file: PPath, stamper: FileStamper? = null)


  /**
   * Returns a logger for logging messages.
   */
  val logger: Logger
}

/**
 * Exception that can occur during task execution.
 */
class ExecException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}
