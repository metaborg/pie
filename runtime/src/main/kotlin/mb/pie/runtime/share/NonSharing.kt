package mb.pie.runtime.share

import mb.pie.api.*

class NonSharingShare : Share {
  override fun <I : In, O : Out> share(key: TaskKey, task: Task<I, O>, execFunc: (Task<I, O>) -> TaskData<I, O>, cacheFunc: (TaskKey) -> UTaskData?): UTaskData {
    return cacheFunc(key) ?: execFunc(task)
  }

  override fun <I : In, O : Out> share(key: TaskKey, task: Task<I, O>, execFunc: (Task<I, O>) -> TaskData<I, O>): UTaskData {
    return execFunc(task)
  }


  override fun toString() = "NonSharingShare"
}
