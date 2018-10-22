package mb.pie.api

import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.vfs.path.PPath

/**
 * Execution context used in execution of [tasks][Task], to require other tasks, and record dependencies to files.
 */
interface ExecContext {
  /**
   * Requires given [task], using the default [output stamper][OutputStamper], and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> require(task: Task<I, O>): O

  /**
   * Requires given [task], using given [output stamper][stamper], and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper): O

  /**
   * Requires task given by its [task definition][taskDef] and [input], using the default [output stamper][OutputStamper], and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I): O

  /**
   * Requires task given by its [task definition][taskDef] and [input], using given [output stamper][stamper], and returns its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper): O

  /**
   * Requires task given by its [serializable task form][task], using the default [output stamper][OutputStamper], and returns its output.
   * Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(task: STask<I>): Out

  /**
   * Requires task given by its [serializable task form][task], using given [output stamper][stamper], and returns its output.
   * Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(task: STask<I>, stamper: OutputStamper): Out

  /**
   * Requires task given by the [identifier of its task definition][taskDefId] and [input], using the default [output stamper][OutputStamper],
   * and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(taskDefId: String, input: I): Out

  /**
   * Requires task given by the [identifier of its task definition][taskDefId] and [input], using given [output stamper][stamper],
   * and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper): Out


  /**
   * Marks given [file] as required (read), using the default [require file stamper][FileStamper], creating a required file dependency.
   */
  fun require(file: PPath)

  /**
   * Marks given [file] as required (read), using given [file stamper][stamper], creating a required file dependency.
   */
  fun require(file: PPath, stamper: FileStamper)

  /**
   * Marks given [file] as provided (written to/created), using the default [provide file stamper][FileStamper], creating a provided file dependency.
   */
  fun generate(file: PPath)

  /**
   * Marks given [file] as provided (written to/created), using given [file stamper][stamper], creating a provided file dependency.
   */
  fun generate(file: PPath, stamper: FileStamper)


  /**
   * Returns a logger for logging messages.
   */
  val logger: Logger
}

/**
 * Exception that can occur during task execution.
 */
class ExecException : Exception {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(cause: Throwable) : super(cause)
}

/**
 * @see ExecContext.require
 */
@Throws(ExecException::class, InterruptedException::class)
fun <I : In, O : Out> ExecContext.require(task: Task<I, O>, stamper: OutputStamper?) =
  if(stamper != null) require(task, stamper) else require(task)

/**
 * @see ExecContext.require
 */
@Throws(ExecException::class, InterruptedException::class)
fun <I : In, O : Out> ExecContext.require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper?) =
  if(stamper != null) require(taskDef, input, stamper) else require(taskDef, input)

/**
 * @see ExecContext.require
 */
@Throws(ExecException::class, InterruptedException::class)
fun <I : In, O : Out> ExecContext.require(task: STask<I>, stamper: OutputStamper?) =
  if(stamper != null) require(task, stamper) else require(task)

/**
 * @see ExecContext.require
 */
@Throws(ExecException::class, InterruptedException::class)
fun <I : In, O : Out> ExecContext.require(taskDefId: String, input: I, stamper: OutputStamper?) =
  if(stamper != null) require(taskDefId, input, stamper) else require(taskDefId, input)

/**
 * @see ExecContext.require
 */
fun ExecContext.require(file: PPath, stamper: FileStamper? = null) =
  if(stamper != null) require(file, stamper) else require(file)

/**
 * @see ExecContext.generate
 */
fun ExecContext.generate(file: PPath, stamper: FileStamper? = null) =
  if(stamper != null) generate(file, stamper) else generate(file)
