package mb.pie.runtime.exec

import mb.fs.api.GeneralFileSystem
import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.fs.stamp.FileSystemStamper
import mb.pie.api.stamp.*

internal class ExecContextImpl(
  private val requireTask: RequireTask,
  private val cancel: Cancelled,
  private val taskDefs: TaskDefs,
  private val generalFileSystem: GeneralFileSystem,
  private val store: Store,
  private val defaultOutputStamper: OutputStamper,
  override val defaultRequireFileSystemStamper: FileSystemStamper,
  override val defaultProvideFileSystemStamper: FileSystemStamper,
  override val logger: Logger
) : ExecContext {
  private val taskRequires = arrayListOf<TaskRequireDep>()
  private val resourceRequires = arrayListOf<ResourceRequireDep>()
  private val resourceProvides = arrayListOf<ResourceProvideDep>()


  override fun <I : In, O : Out> require(task: Task<I, O>): O {
    return require(task, defaultOutputStamper)
  }

  override fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper): O {
    cancel.throwIfCancelled()
    val key = task.key()
    val output = requireTask.require(key, task, cancel)
    val stamp = stamper.stamp(output)
    taskRequires.add(TaskRequireDep(key, stamp))
    Stats.addCallReq()
    return output
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I): O {
    return require(Task(taskDef, input), defaultOutputStamper)
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper): O {
    return require(Task(taskDef, input), stamper)
  }

  override fun <I : In> require(task: STask<I>): Out {
    return require(task.toTask(taskDefs), defaultOutputStamper)
  }

  override fun <I : In> require(task: STask<I>, stamper: OutputStamper): Out {
    return require(task.toTask(taskDefs), stamper)
  }

  override fun <I : In> require(taskDefId: String, input: I): Out {
    val taskDef = getTaskDef<I>(taskDefId)
    return require(Task(taskDef, input), defaultOutputStamper)
  }

  override fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper): Out {
    val taskDef = getTaskDef<I>(taskDefId)
    return require(Task(taskDef, input), stamper)
  }

  private fun <I : In> getTaskDef(id: String) = taskDefs.getTaskDef<I, Out>(id)
    ?: throw RuntimeException("Cannot retrieve task with identifier $id, it cannot be found")


  override fun <R : Resource> require(resource: R, stamper: ResourceStamper<R>) {
    @Suppress("UNCHECKED_CAST") val stamp = stamper.stamp(resource) as ResourceStamp<Resource>
    resourceRequires.add(ResourceRequireDep(resource.key(), stamp))
    Stats.addFileReq()
  }

  override fun <R : Resource> provide(resource: R, stamper: ResourceStamper<R>) {
    @Suppress("UNCHECKED_CAST") val stamp = stamper.stamp(resource) as ResourceStamp<Resource>
    resourceProvides.add(ResourceProvideDep(resource.key(), stamp))
    Stats.addFileGen()
  }


  override fun require(path: FSPath): FSNode {
    return require(path, defaultRequireFileSystemStamper)
  }

  override fun require(path: FSPath, stamper: FileSystemStamper): FSNode {
    val node = generalFileSystem.getNode(path)
    require(node, stamper)
    return node
  }

  override fun provide(path: FSPath) {
    return provide(path, defaultProvideFileSystemStamper)
  }

  override fun provide(path: FSPath, stamper: FileSystemStamper) {
    val node = generalFileSystem.getNode(path)
    provide(node, stamper)
  }


  override fun toNode(path: FSPath): FSNode {
    return generalFileSystem.getNode(path)
  }


  data class Deps(val taskRequires: ArrayList<TaskRequireDep>, val resourceRequires: ArrayList<ResourceRequireDep>, val resourceProvides: ArrayList<ResourceProvideDep>)

  fun deps() = Deps(taskRequires, resourceRequires, resourceProvides)
}
