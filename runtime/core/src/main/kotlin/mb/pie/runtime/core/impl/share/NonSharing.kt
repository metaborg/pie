package mb.pie.runtime.core.impl.share

import mb.pie.runtime.core.*


class NonSharingShare : Share {
  override fun reuseOrCreate(task: UTask, cacheFunc: (UTask) -> UTaskData?, execFunc: (UTask) -> UTaskData): UTaskData {
    return cacheFunc(task) ?: execFunc(task)
  }

  override fun reuseOrCreate(task: UTask, execFunc: (UTask) -> UTaskData): UTaskData {
    return execFunc(task)
  }


  override fun toString() = "NonSharingShare"
}
