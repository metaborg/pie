package mb.pie.runtime

import mb.pie.runtime.stamp.*
import mb.vfs.path.PPath


/**
 * Internal execution context, used in execution of a [TaskDef].
 */
interface ExecContext {
  /**
   * Requires given task, returning its output, and creates a task dependency. By default, uses the equality stamper for change detection.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireOutput(task: Task<I, O>, stamper: OutputStamper = OutputStampers.equals): O

  /**
   * Requires a task, constructed from given task identifier and input, returning its output, and creates a task dependency.
   * The class is used for type inference. By default, uses the equality stamper for change detection.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : TaskDef<I, O>> requireOutput(clazz: Class<F>, id: String, input: I, stamper: OutputStamper = OutputStampers.equals): O =
    requireOutput(Task<I, O>(id, input), stamper)


  /**
   * Requires execution of given task, ignoring its output, and creates a task dependency.
   * By default, no change detection is performed, since there is no output object.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun requireExec(task: UTask, stamper: OutputStamper = OutputStampers.inconsequential)

  /**
   * Requires execution of a task, constructed from given task identifier and input, ignoring its output, and creates a task dependency.
   * The class is used for type inference. By default, no change detection is performed, since there is no output object.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : TaskDef<I, O>> requireExec(clazz: Class<F>, id: String, input: I, stamper: OutputStamper = OutputStampers.inconsequential) {
    requireExec(Task<I, O>(id, input), stamper)
  }


  /**
   * Marks given file as required (read), creating a required file dependency. By default, uses the modification date stamper for change detection.
   */
  fun require(file: PPath, stamper: FileStamper = FileStampers.modified)

  /**
   * Marks given file as generated (written to/created), creating a generated file dependency. By default, uses the hash stamper for change detection.
   */
  fun generate(file: PPath, stamper: FileStamper = FileStampers.hash)
}

/**
 * Exception that can occur during task execution.
 */
class ExecException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}
