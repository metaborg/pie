package mb.pie.runtime.share

import mb.pie.api.*

class NonSharingShare : Share {
  override fun reuseOrCreate(key: TaskKey, cacheFunc: (TaskKey) -> UTaskData?, execFunc: (UTask) -> UTaskData): UTaskData {
    return cacheFunc(key) ?: execFunc(key)
  }

  override fun reuseOrCreate(key: TaskKey, execFunc: (UTask) -> UTaskData): UTaskData {
    return execFunc(key)
  }


  override fun toString() = "NonSharingShare"
}
