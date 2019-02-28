package mb.pie.runtime.exec

import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.fs.FileSystemResource
import mb.pie.api.fs.ResourceUtils
import mb.pie.api.stamp.*

internal class ExecContextImpl : ExecContext {
  private val requireTask: RequireTask;
  private val cancel: Cancelled;
  private val taskDefs: TaskDefs;
  private val resourceSystems: ResourceSystems;
  private val store: Store;
  private val defaultOutputStamper: OutputStamper;
  private val defaultRequireFileSystemStamper: FileSystemStamper;
  private val defaultProvideFileSystemStamper: FileSystemStamper;
  private val logger: Logger;

  private val taskRequires: ArrayList<TaskRequireDep> = ArrayList<TaskRequireDep>();
  private val resourceRequires: ArrayList<ResourceRequireDep> = ArrayList<ResourceRequireDep>();
  private val resourceProvides: ArrayList<ResourceProvideDep> = ArrayList<ResourceProvideDep>();


  public constructor(
    requireTask: RequireTask,
    cancel: Cancelled,
    taskDefs: TaskDefs,
    resourceSystems: ResourceSystems,
    store: Store,
    defaultOutputStamper: OutputStamper,
    defaultRequireFileSystemStamper: FileSystemStamper,
    defaultProvideFileSystemStamper: FileSystemStamper,
    logger: Logger
  ) {
    this.requireTask = requireTask;
    this.cancel = cancel;
    this.taskDefs = taskDefs;
    this.resourceSystems = resourceSystems;
    this.store = store;
    this.defaultOutputStamper = defaultOutputStamper;
    this.defaultRequireFileSystemStamper = defaultRequireFileSystemStamper;
    this.defaultProvideFileSystemStamper = defaultProvideFileSystemStamper;
    this.logger = logger;
  }


  override fun <I : In, O : Out> require(task: Task<I, O>): O {
    return require(task, defaultOutputStamper);
  }

  override fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper): O {
    cancel.throwIfCancelled();
    val key: TaskKey = task.key();
    val output: O = requireTask.require(key, task, cancel);
    val stamp: OutputStamp = stamper.stamp(output);
    taskRequires.add(TaskRequireDep(key, stamp));
    Stats.addCallReq();
    return output;
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I): O {
    return require(Task(taskDef, input), defaultOutputStamper);
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper): O {
    return require(Task(taskDef, input), stamper);
  }

  override fun <I : In> require(task: STask<I>): Out {
    return require(task.toTask(taskDefs), defaultOutputStamper);
  }

  override fun <I : In> require(task: STask<I>, stamper: OutputStamper): Out {
    return require(task.toTask(taskDefs), stamper);
  }

  override fun <I : In> require(taskDefId: String, input: I): Out {
    val taskDef: TaskDef<I, Out> = getTaskDef<I>(taskDefId)
    return require(Task(taskDef, input), defaultOutputStamper);
  }

  override fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper): Out {
    val taskDef: TaskDef<I, Out> = getTaskDef<I>(taskDefId);
    return require(Task(taskDef, input), stamper);
  }

  private fun <I : In> getTaskDef(id: String): TaskDef<I, Out> {
    val taskDef: TaskDef<I, Out>? = taskDefs.getTaskDef<I, Out>(id);
    if(taskDef != null) {
      return taskDef;
    } else {
      throw RuntimeException("Cannot retrieve task with identifier '" + id + "', it cannot be found");
    }
  }


  override fun <R : Resource> require(resource: R, stamper: ResourceStamper<R>) {
    val stamp: ResourceStamp<Resource> = stamper.stamp(resource) as ResourceStamp<Resource>;
    resourceRequires.add(ResourceRequireDep(resource.getKey(), stamp));
    Stats.addFileReq();
  }

  override fun <R : Resource> provide(resource: R, stamper: ResourceStamper<R>) {
    val stamp: ResourceStamp<Resource> = stamper.stamp(resource) as ResourceStamp<Resource>;
    resourceProvides.add(ResourceProvideDep(resource.getKey(), stamp));
    Stats.addFileGen();
  }


  override fun require(path: FSPath): FSNode {
    return require(path, defaultRequireFileSystemStamper);
  }

  override fun require(path: FSPath, stamper: FileSystemStamper): FSNode {
    val fileSystemId = path.getFileSystemId();
    val resourceSystem: ResourceSystem? = resourceSystems.getResourceSystem(fileSystemId);
    if(resourceSystem == null) {
      throw RuntimeException("Cannot get resource system for path " + path + "; resource system with id '" + fileSystemId + "' does not exist");
    }
    val node: FSNode = ResourceUtils.toNode(path, resourceSystem);
    require(node, stamper);
    return node;
  }

  override fun provide(path: FSPath) {
    return provide(path, defaultProvideFileSystemStamper);
  }

  override fun provide(path: FSPath, stamper: FileSystemStamper) {
    val fileSystemId = path.getFileSystemId();
    val resourceSystem: ResourceSystem? = resourceSystems.getResourceSystem(fileSystemId);
    if(resourceSystem == null) {
      throw RuntimeException("Cannot get resource system for path " + path + "; resource system with id '" + fileSystemId + "' does not exist");
    }
    val node: FSNode = ResourceUtils.toNode(path, resourceSystem);
    provide(node, stamper);
  }


  override fun toNode(path: FSPath): FSNode {
    val fileSystemId = path.getFileSystemId();
    val resourceSystem: ResourceSystem? = resourceSystems.getResourceSystem(fileSystemId);
    if(resourceSystem == null) {
      throw RuntimeException("Cannot get resource system for path " + path + "; resource system with id '" + fileSystemId + "' does not exist");
    }
    return ResourceUtils.toNode(path, resourceSystem);
  }


  override fun defaultRequireFileSystemStamper(): ResourceStamper<FileSystemResource> {
    return defaultRequireFileSystemStamper;
  }

  override fun defaultProvideFileSystemStamper(): ResourceStamper<FileSystemResource> {
    return defaultProvideFileSystemStamper;
  }

  override fun logger(): Logger {
    return logger;
  }


  class Deps {
    val taskRequires: ArrayList<TaskRequireDep>;
    val resourceRequires: ArrayList<ResourceRequireDep>;
    val resourceProvides: ArrayList<ResourceProvideDep>;

    constructor(taskRequires: ArrayList<TaskRequireDep>, resourceRequires: ArrayList<ResourceRequireDep>, resourceProvides: ArrayList<ResourceProvideDep>) {
      this.taskRequires = taskRequires;
      this.resourceRequires = resourceRequires;
      this.resourceProvides = resourceProvides;
    }
  }

  fun deps(): Deps {
    return Deps(taskRequires, resourceRequires, resourceProvides);
  }
}
