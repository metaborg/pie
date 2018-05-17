package mb.pie.api

import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.vfs.path.PPath

/**
 * Execution context used in execution of [tasks][Task], to require other tasks, and record dependencies to files.
 */
interface ExecContext {
  /**
   * Requires given task, returning its output, and creates a task dependency.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireOutput(task: Task<I, O>, stamper: OutputStamper? = null): O

  /**
   * Requires a task, constructed from given task identifier and input, returning its output, and creates a task dependency.
   * The class is used for type inference.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : TaskDef<I, O>> requireOutput(clazz: Class<F>, id: String, input: I, stamper: OutputStamper? = null): O =
    requireOutput(Task<I, O>(id, input), stamper)


  /**
   * Requires execution of given task, ignoring its output, and creates a task dependency. No change detection is performed, since the
   * output object is ignored.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun requireExec(task: UTask)

  /**
   * Requires execution of a task, constructed from given task identifier and input, ignoring its output, and creates a task dependency.
   * The class is used for type inference. By default, no change detection is performed, since the output object is ignored.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : TaskDef<I, O>> requireExec(clazz: Class<F>, id: String, input: I) {
    requireExec(Task<I, O>(id, input))
  }


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
