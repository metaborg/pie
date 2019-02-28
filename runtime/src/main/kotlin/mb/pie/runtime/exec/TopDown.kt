package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.api.stamp.OutputStamper
import java.util.ArrayList
import java.util.function.Function
import kotlin.collections.HashMap

public class TopDownExecutorImpl : TopDownExecutor {
  private val taskDefs: TaskDefs;
  private val resourceSystems: ResourceSystems;
  private val store: Store;
  private val share: Share;
  private val defaultOutputStamper: OutputStamper;
  private val defaultRequireFileSystemStamper: FileSystemStamper;
  private val defaultProvideFileSystemStamper: FileSystemStamper;
  private val layerFactory: Function<Logger, Layer>;
  private val logger: Logger;
  private val executorLoggerFactory: Function<Logger, ExecutorLogger>;

  public constructor(
    taskDefs: TaskDefs,
    resourceSystems: ResourceSystems,
    store: Store,
    share: Share,
    defaultOutputStamper: OutputStamper,
    defaultRequireFileSystemStamper: FileSystemStamper,
    defaultProvideFileSystemStamper: FileSystemStamper,
    layerFactory: Function<Logger, Layer>,
    logger: Logger,
    executorLoggerFactory: Function<Logger, ExecutorLogger>
  ) {
    this.taskDefs = taskDefs;
    this.resourceSystems = resourceSystems;
    this.store = store;
    this.share = share;
    this.defaultOutputStamper = defaultOutputStamper;
    this.defaultRequireFileSystemStamper = defaultRequireFileSystemStamper;
    this.defaultProvideFileSystemStamper = defaultProvideFileSystemStamper;
    this.layerFactory = layerFactory;
    this.logger = logger;
    this.executorLoggerFactory = executorLoggerFactory;
  }

  override fun newSession(): TopDownSession {
    return TopDownSessionImpl(taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory.apply(logger), logger, executorLoggerFactory.apply(logger));
  }
}

open class TopDownSessionImpl : TopDownSession, RequireTask {
  private val store: Store;
  private val layer: Layer;
  private val executorLogger: ExecutorLogger;
  private val executor: TaskExecutor;
  private val requireShared: RequireShared;

  private val visited: HashMap<TaskKey, TaskData<*, *>> = HashMap<TaskKey, TaskData<*, *>>();


  constructor(
    taskDefs: TaskDefs,
    resourceSystems: ResourceSystems,
    store: Store,
    share: Share,
    defaultOutputStamper: OutputStamper,
    defaultRequireFileSystemStamper: FileSystemStamper,
    defaultProvideFileSystemStamper: FileSystemStamper,
    layer: Layer,
    logger: Logger,
    executorLogger: ExecutorLogger
  ) {
    this.store = store;
    this.layer = layer;
    this.executorLogger = executorLogger;
    this.executor = TaskExecutor(taskDefs, resourceSystems, visited, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layer, logger, executorLogger, null);
    this.requireShared = RequireShared(taskDefs, resourceSystems, visited, store, executorLogger);
  }


  override fun <I : In, O : Out> requireInitial(task: Task<I, O>): O {
    return requireInitial(task, NullCancelled());
  }

  override fun <I : In, O : Out> requireInitial(task: Task<I, O>, cancel: Cancelled): O {
    try {
      val key: TaskKey = task.key();
      executorLogger.requireTopDownInitialStart(key, task);
      val output: O = require(key, task, cancel);
      executorLogger.requireTopDownInitialEnd(key, task, output);
      return output;
    } finally {
      store.sync();
    }
  }

  override fun <I : In, O : Out> require(key: TaskKey, task: Task<I, O>, cancel: Cancelled): O {
    cancel.throwIfCancelled();
    Stats.addRequires();
    layer.requireTopDownStart(key, task.input);
    executorLogger.requireTopDownStart(key, task);
    try {
      val status: DataAndExecutionStatus<I, O> = executeOrGetExisting(key, task, cancel);
      val data: TaskData<I, O> = status.data;
      val output: O? = data.output;
      if(!status.executed) {
        // Validate well-formedness of the dependency graph.
        store.readTxn().use { txn: StoreReadTxn -> layer.validatePostWrite(key, data, txn) };
        // Mark task as visited.
        visited.set(key, data);
      }
      executorLogger.requireTopDownEnd(key, task, output);
      return output as O; // TODO: can return null.
    } finally {
      layer.requireTopDownEnd(key);
    }
  }

  private class DataAndExecutionStatus<I : In, O : Out> {
    val data: TaskData<I, O>;
    val executed: Boolean;

    constructor(data: TaskData<I, O>, executed: Boolean) {
      this.data = data;
      this.executed = executed;
    }
  }

  /**
   * Get data for given task/key, either by getting existing data or through execution.
   */
  private fun <I : In, O : Out> executeOrGetExisting(key: TaskKey, task: Task<I, O>, cancel: Cancelled): DataAndExecutionStatus<I, O> {
    // Check if task was already visited this execution. Return immediately if so.
    val visitedData: TaskData<*, *>? = requireShared.dataFromVisited(key);
    if(visitedData != null) {
      return DataAndExecutionStatus(visitedData.cast<I, O>(), false);
    }

    // Check if data is stored for task. Execute if not.
    val storedData: TaskData<*, *>? = requireShared.dataFromStore(key);
    if(storedData == null) {
      return DataAndExecutionStatus(exec(key, task, NoData(), cancel), true);
    }

    // Check consistency of task.
    val existingData: TaskData<I, O> = storedData.cast<I, O>();
    val input: I = existingData.input;
    val output: O? = existingData.output;
    val taskRequires: ArrayList<TaskRequireDep> = existingData.taskRequires;
    val resourceRequires: ArrayList<ResourceRequireDep> = existingData.resourceRequires;
    val resourceProvides: ArrayList<ResourceProvideDep> = existingData.resourceProvides;

    // Internal consistency: input changes.
    run {
      val reason: InconsistentInput? = requireShared.checkInput(input, task);
      if(reason != null) {
        return DataAndExecutionStatus(exec(key, task, reason, cancel), true);
      }
    }

    // Internal consistency: transient output consistency.
    run {
      val reason: InconsistentTransientOutput? = requireShared.checkOutputConsistency(output);
      if(reason != null) {
        return DataAndExecutionStatus(exec(key, task, reason, cancel), true);
      }
    }

    // Internal consistency: resoruce requires.
    for(fileReq in resourceRequires) {
      val reason: InconsistentResourceRequire? = requireShared.checkResourceRequire(key, task, fileReq);
      if(reason != null) {
        return DataAndExecutionStatus(exec(key, task, reason, cancel), true);
      }
    }

    // Internal consistency: resource provides.
    for(fileGen in resourceProvides) {
      val reason: InconsistentResourceProvide? = requireShared.checkResourceProvide(key, task, fileGen);
      if(reason != null) {
        return DataAndExecutionStatus(exec(key, task, reason, cancel), true);
      }
    }

    // Total consistency: call requirements.
    for(taskReq in taskRequires) {
      val reason: InconsistentTaskReq? = requireShared.checkTaskRequire(key, task, taskReq, this, cancel);
      if(reason != null) {
        return DataAndExecutionStatus(exec(key, task, reason, cancel), true);
      }
    }

    // Task is consistent.
    return DataAndExecutionStatus(existingData, false);
  }

  open fun <I : In, O : Out> exec(key: TaskKey, task: Task<I, O>, reason: ExecReason, cancel: Cancelled): TaskData<I, O> {
    return executor.exec(key, task, reason, this, cancel);
  }
}
