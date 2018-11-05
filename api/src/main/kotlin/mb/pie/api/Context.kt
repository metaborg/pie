package mb.pie.api

import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.fs.java.JavaFSNode
import mb.fs.java.JavaFSPath
import mb.pie.api.fs.stamp.FileSystemStamper
import mb.pie.api.fs.toNode
import mb.pie.api.fs.toResource
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import java.io.File
import java.nio.file.Path

/**
 * Execution context used in execution of [tasks][Task], to require other tasks, and to record dependencies to resources.
 */
interface ExecContext {
  //
  // Executing and recording dependencies to tasks.
  //

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
   * Requires task given by its [task definition][taskDef] and [input], using the default [output stamper][OutputStamper], and returns its
   * output.
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
   * Requires task given by the [identifier of its task definition][taskDefId] and [input], using the default
   * [output stamper][OutputStamper], and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or
   * [TaskDef] if possible.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(taskDefId: String, input: I): Out

  /**
   * Requires task given by the [identifier of its task definition][taskDefId] and [input], using given [output stamper][stamper],
   * and returns its output. Requires lookup and cast of a task definition, prefer [require] with [Task] or [TaskDef] if possible.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper): Out


  //
  // Recording dependencies to resources.
  //


  /**
   * Marks given [resource] as required (read), using given [resource stamper][stamper], creating a required resource dependency.
   */
  fun <R : Resource> require(resource: R, stamper: ResourceStamper<R>)

  /**
   * Marks given [resource] as provided (written to/created), using given [resource stamper][stamper], creating a provided resource
   * dependency. The current contents of the resource may be used for change detection, so be sure to call [provide] AFTER modifying the
   * resource.
   */
  fun <R : Resource> provide(resource: R, stamper: ResourceStamper<R>)


  //
  // Recording required (read) dependencies to files and directories of file systems.
  //


  /**
   * Marks given [file system path][path] as required (read), using the
   * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
   *
   * @return resolved file system node.
   */
  fun require(path: FSPath): FSNode

  /**
   * Marks given [file system path][path] as required (read), using given [file system stamper][stamper], creating a required resource
   * dependency.
   *
   * @return resolved file system node.
   */
  fun require(path: FSPath, stamper: FileSystemStamper): FSNode

  /**
   * Marks given [file system node][node] as required (read), using the
   * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
   */
  @JvmDefault
  fun require(node: FSNode) = require(node.toResource(), defaultRequireFileSystemStamper)

  /**
   * Marks given [file system node][node] as required (read), using given [file system stamper][stamper], creating a required resource
   * dependency.
   */
  @JvmDefault
  fun require(node: FSNode, stamper: FileSystemStamper) = require(node.toResource(), stamper)

  /**
   * Marks given [Java file system path][path] as required (read), using the
   * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
   *
   * @return resolved Java file system node.
   */
  @JvmDefault
  fun require(path: JavaFSPath): JavaFSNode = path.toNode().also { require(it, defaultRequireFileSystemStamper) }

  /**
   * Marks given [Java file system path][path] as required (read), using given [file system stamper][stamper], creating a required
   * resource dependency.
   *
   * @return resolved Java file system node.
   */
  @JvmDefault
  fun require(path: JavaFSPath, stamper: FileSystemStamper): JavaFSNode = path.toNode().also { require(it, stamper) }

  /**
   * Marks given [Java file system node][node] as required (read), using the
   * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
   */
  @JvmDefault
  fun require(node: JavaFSNode) = require(node.toResource(), defaultRequireFileSystemStamper)

  /**
   * Marks given [Java file system node][node] as required (read), using given [file system stamper][stamper], creating a required
   * resource dependency.
   */
  @JvmDefault
  fun require(node: JavaFSNode, stamper: FileSystemStamper) = require(node.toResource(), stamper)

  /**
   * Marks given [Java file system (java.nio.file) path][path] as required (read), using the
   * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
   */
  @JvmDefault
  fun require(path: Path) = require(path.toResource(), defaultRequireFileSystemStamper)

  /**
   * Marks given [Java file system (java.nio.file) path][path] as required (read), using given [file system stamper][stamper], creating a
   * required resource dependency.
   */
  @JvmDefault
  fun require(path: Path, stamper: FileSystemStamper) = require(path.toResource(), stamper)

  /**
   * Marks given [Java local file (java.io) path][file] as required (read), using the
   * [default 'require' file system stamper][defaultRequireFileSystemStamper], creating a required resource dependency.
   */
  @JvmDefault
  fun require(file: File) = require(file.toResource(), defaultRequireFileSystemStamper)

  /**
   * Marks given [Java local file (java.io) path][file] as required (read), using given [file system stamper][stamper], creating a required
   * resource dependency.
   */
  @JvmDefault
  fun require(file: File, stamper: FileSystemStamper) = require(file.toResource(), stamper)

  /**
   * Default 'require' file system stamper.
   */
  val defaultRequireFileSystemStamper: FileSystemStamper


  //
  // Recording provided (written to/created) dependencies to files and directories of file systems.
  //


  /**
   * Marks given [file system path][path] as provided (written to/created), using the
   * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
   * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
   */
  fun provide(path: FSPath)

  /**
   * Marks given [file system path][path] as provided (written to/created), using given [file system stamper][stamper], creating a provided
   * resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call [provide] AFTER
   * writing to the file or directory.
   */
  fun provide(path: FSPath, stamper: FileSystemStamper)

  /**
   * Marks given [file system node][node] as provided (written to/created), using the
   * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
   * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(node: FSNode) = provide(node.toResource(), defaultProvideFileSystemStamper)

  /**
   * Marks given [file system node][node] as provided (written to/created), using given [file system stamper][stamper], creating a provided
   * resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call [provide] AFTER
   * writing to the file or directory.
   */
  @JvmDefault
  fun provide(node: FSNode, stamper: FileSystemStamper) = provide(node.toResource(), stamper)

  /**
   * Marks given [Java file system path][path] as provided (written to/created), using the
   * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
   * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(path: JavaFSPath) = provide(path.toResource(), defaultProvideFileSystemStamper)

  /**
   * Marks given [Java file system path][path] as provided (written to/created), using given [file system stamper][stamper], creating a
   * provided resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call
   * [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(path: JavaFSPath, stamper: FileSystemStamper) = provide(path.toResource(), stamper)

  /**
   * Marks given [Java file system node][node] as provided (written to/created), using the
   * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
   * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(node: JavaFSNode) = provide(node.toResource(), defaultProvideFileSystemStamper)

  /**
   * Marks given [Java files ystem node][node] as provided (written to/created), using given [file system stamper][stamper], creating a
   * provided resource dependency. The current contents of the file or directory may be used for change detection, so be sure to call
   * [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(node: JavaFSNode, stamper: FileSystemStamper) = provide(node.toResource(), stamper)

  /**
   * Marks given [Java file system (java.nio.file) path][path] as provided (written to/created), using the
   * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
   * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(path: Path) = provide(path.toResource(), defaultProvideFileSystemStamper)

  /**
   * Marks given [Java file system (java.nio.file) path][path] as provided (written to/created), using given
   * [file system stamper][stamper], creating a provided resource dependency. The current contents of the file or directory may be used for
   * change detection, so be sure to call [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(path: Path, stamper: FileSystemStamper) = provide(path.toResource(), stamper)

  /**
   * Marks given [Java local file (java.io) path][file] as provided (written to/created), using the
   * [default 'provide' file system stamper][defaultProvideFileSystemStamper], creating a provided resource dependency. The current contents
   * of the file or directory may be used for change detection, so be sure to call [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(file: File) = provide(file.toResource(), defaultProvideFileSystemStamper)

  /**
   * Marks given [Java local file (java.io) path][file] as provided (written to/created), using given [file system stamper][stamper],
   * creating a provided resource dependency. The current contents of the file or directory may be used for change detection, so be sure to
   * call [provide] AFTER writing to the file or directory.
   */
  @JvmDefault
  fun provide(file: File, stamper: FileSystemStamper) = provide(file.toResource(), stamper)

  /**
   * Default 'provide' file system stamper.
   */
  val defaultProvideFileSystemStamper: FileSystemStamper


  //
  // Resolving file system paths to file system nodes.
  //


  /**
   * Resolves a file system path into a file system node, providing I/O. Does not create a dependency, use [require] or [provide] to record
   * a dependency.
   *
   * @return resolved file system node.
   */
  fun toNode(path: FSPath): FSNode

  /**
   * Resolves a Java file system path into a Java file system node, providing I/O. Does not create a dependency, use [require] or [provide]
   * to record a dependency.
   *
   * @return resolved Java file system node.
   */
  @JvmDefault
  fun toNode(path: JavaFSPath): JavaFSNode = JavaFSNode(path)


  //
  // Logging.
  //


  /**
   * Logger.
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
fun <I : In> ExecContext.require(task: STask<I>, stamper: OutputStamper?) =
  if(stamper != null) require(task, stamper) else require(task)

/**
 * @see ExecContext.require
 */
@Throws(ExecException::class, InterruptedException::class)
fun <I : In> ExecContext.require(taskDefId: String, input: I, stamper: OutputStamper?) =
  if(stamper != null) require(taskDefId, input, stamper) else require(taskDefId, input)
