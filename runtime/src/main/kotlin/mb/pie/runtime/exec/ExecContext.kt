package mb.pie.runtime.exec

import mb.fs.api.GeneralFileSystem
import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper

internal class ExecContextImpl(
  private val requireTask: RequireTask,
  private val cancel: Cancelled,
  private val taskDefs: TaskDefs,
  private val generalFileSystem: GeneralFileSystem,
  private val store: Store,
  private val defaultOutputStamper: OutputStamper,
  private val defaultResourceRequireStamper: ResourceStamper,
  private val defaultResourceProvideStamper: ResourceStamper,
  override val logger: Logger
) : ExecContext {
  private val taskReqs = arrayListOf<TaskReq>()
  private val fileReqs = arrayListOf<ResourceRequire>()
  private val fileGens = arrayListOf<ResourceProvide>()


  override fun <I : In, O : Out> require(task: Task<I, O>): O {
    return require(task, defaultOutputStamper)
  }

  override fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper): O {
    cancel.throwIfCancelled()
    val key = task.key()
    val output = requireTask.require(key, task, cancel)
    val stamp = stamper.stamp(output)
    taskReqs.add(TaskReq(key, stamp))
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


  override fun require(resource: Resource) {
    require(resource, defaultResourceRequireStamper)
  }

  override fun require(resource: Resource, stamper: ResourceStamper) {
    val stamp = stamper.stamp(resource)
    fileReqs.add(ResourceRequire(resource.key(), stamp))
    Stats.addFileReq()
  }

  override fun require(path: FSPath): FSNode {
    return require(path, defaultResourceProvideStamper)
  }

  override fun require(path: FSPath, stamper: ResourceStamper): FSNode {
    val node = generalFileSystem.getNode(path)
    require(node, stamper)
    return node
  }


  override fun provide(resource: Resource) {
    return provide(resource, defaultResourceProvideStamper)
  }

  override fun provide(resource: Resource, stamper: ResourceStamper) {
    val stamp = stamper.stamp(resource)
    fileGens.add(ResourceProvide(resource.key(), stamp))
    Stats.addFileGen()
  }

  override fun provide(path: FSPath) {
    return provide(path, defaultResourceProvideStamper)
  }

  override fun provide(path: FSPath, stamper: ResourceStamper) {
    val node = generalFileSystem.getNode(path)
    provide(node, stamper)
  }


  override fun toNode(path: FSPath): FSNode {
    return generalFileSystem.getNode(path)
  }


  data class Reqs(val taskReqs: ArrayList<TaskReq>, val fileReqs: ArrayList<ResourceRequire>, val fileGens: ArrayList<ResourceProvide>)

  fun reqs() = Reqs(taskReqs, fileReqs, fileGens)
}
