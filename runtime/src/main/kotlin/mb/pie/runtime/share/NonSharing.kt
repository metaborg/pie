package mb.pie.runtime.share

import mb.pie.api.*

class NonSharingShare : Share {
  @Suppress("OVERRIDE_BY_INLINE")
  override inline fun share(key: TaskKey, crossinline execFunc: () -> TaskData<*, *>, crossinline visitedFunc: () -> TaskData<*, *>?): TaskData<*, *> {
    return visitedFunc() ?: execFunc()
  }

  override fun toString() = "NonSharingShare"
}
