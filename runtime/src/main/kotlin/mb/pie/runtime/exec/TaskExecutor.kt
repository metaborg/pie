package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.exec.ExecReason
import mb.pie.api.stamp.OutputStamper
import mb.pie.runtime.share.NonSharingShare
import java.util.function.BiConsumer
import java.util.function.Supplier

public class TaskExecutor {
  private val taskDefs: TaskDefs;
  private val resourceSystems: ResourceSystems;
  private val visited: MutableMap<TaskKey, TaskData<*, *>>;
  private val store: Store;
  private val share: Share;
  private val defaultOutputStamper: OutputStamper;
  private val defaultRequireFileSystemStamper: FileSystemStamper;
  private val defaultProvideFileSystemStamper: FileSystemStamper;
  private val layer: Layer;
  private val logger: Logger;
  private val executorLogger: ExecutorLogger;
  private val postExecFunc: BiConsumer<TaskKey, TaskData<*, *>>?;

  public constructor(
    taskDefs: TaskDefs,
    resourceSystems: ResourceSystems,
    visited: MutableMap<TaskKey, TaskData<*, *>>,
    store: Store,
    share: Share,
    defaultOutputStamper: OutputStamper,
    defaultRequireFileSystemStamper: FileSystemStamper,
    defaultProvideFileSystemStamper: FileSystemStamper,
    layer: Layer,
    logger: Logger,
    executorLogger: ExecutorLogger,
    postExecFunc: BiConsumer<TaskKey, TaskData<*, *>>?
  ) {
    this.taskDefs = taskDefs;
    this.resourceSystems = resourceSystems;
    this.visited = visited;
    this.store = store;
    this.share = share;
    this.defaultOutputStamper = defaultOutputStamper;
    this.defaultRequireFileSystemStamper = defaultRequireFileSystemStamper;
    this.defaultProvideFileSystemStamper = defaultProvideFileSystemStamper;
    this.layer = layer;
    this.logger = logger;
    this.executorLogger = executorLogger;
    this.postExecFunc = postExecFunc;
  }


  fun <I : In, O : Out> exec(key: TaskKey, task: Task<I, O>, reason: ExecReason, requireTask: RequireTask, cancel: Cancelled): TaskData<I, O> {
    cancel.throwIfCancelled();
    executorLogger.executeStart(key, task, reason);
    val data: TaskData<*, *>;
    if(share is NonSharingShare) {
      // PERF HACK: circumvent share if it is a NonSharingShare for performance.
      data = execInternal(key, task, requireTask, cancel);
    } else {
      data = share.share(key, Supplier { execInternal(key, task, requireTask, cancel) }, Supplier { visited.get(key) });
    }
    executorLogger.executeEnd(key, task, reason, data);
    return data.cast<I, O>();
  }

  private fun <I : In, O : Out> execInternal(key: TaskKey, task: Task<I, O>, requireTask: RequireTask, cancel: Cancelled): TaskData<I, O> {
    cancel.throwIfCancelled();
    // Execute
    val context = ExecContextImpl(requireTask, cancel, taskDefs, resourceSystems, store, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, logger);
    val output: O? = task.exec(context);
    Stats.addExecution();
    val deps: ExecContextImpl.Deps = context.deps();
    val data: TaskData<I, O> = TaskData(task.input, output, deps.taskRequires, deps.resourceRequires, deps.resourceProvides);
    // Validate well-formedness of the dependency graph, before writing.
    store.readTxn().use { txn: StoreReadTxn ->
      layer.validatePreWrite(key, data, txn);
    }
    // Call post-execution function.
    if(postExecFunc != null) {
      postExecFunc.accept(key, data)
    };
    // Write output and dependencies to the store.
    store.writeTxn().use { txn: StoreWriteTxn ->
      txn.setData(key, data);
    }
    // Validate well-formedness of the dependency graph, after writing.
    store.readTxn().use { txn: StoreReadTxn ->
      layer.validatePostWrite(key, data, txn);
    }
    // Mark as visited.
    visited.set(key, data);
    return data;
  }
}
