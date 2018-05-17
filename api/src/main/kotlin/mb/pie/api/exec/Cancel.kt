package mb.pie.api.exec

/**
 * Interface for requesting cancellation.
 */
interface Cancel {
  /**
   * Request cancellation.
   */
  fun requestCancel()
}

/**
 * Interface for checking if an operation has been cancelled.
 */
interface Cancelled {
  /**
   * @return If cancellation has been requested.
   */
  val isCancelled: Boolean

  /**
   * @throws InterruptedException When cancellation has been requested.
   */
  @Throws(InterruptedException::class)
  fun throwIfCancelled()
}

/**
 * Simple cancellation token implementation.
 */
class CancelToken : Cancel, Cancelled {
  @Volatile
  private var cancel = false

  override fun requestCancel() {
    cancel = true
  }

  override val isCancelled: Boolean
    get() {
      return cancel
    }

  @Throws(InterruptedException::class)
  override fun throwIfCancelled() {
    if(cancel) {
      throw InterruptedException()
    }
  }
}

/**
 * Thread interrupt cancellation token implementation.
 */
class InterruptCancelToken @JvmOverloads constructor(
  private val thread: Thread = Thread.currentThread()
) : Cancel, Cancelled {
  override fun requestCancel() {
    thread.interrupt()
  }

  override val isCancelled: Boolean = thread.isInterrupted

  @Throws(InterruptedException::class)
  override fun throwIfCancelled() {
    if(isCancelled) {
      throw InterruptedException()
    }
  }
}

/**
 * Cancelled implementation that never cancels.
 */
class NullCancelled : Cancelled {
  override val isCancelled: Boolean = false
  override fun throwIfCancelled() {}
}

/**
 * Cancellation token implementation that never cancels.
 */
class NullCancelToken : Cancel, Cancelled {
  override fun requestCancel() {}
  override val isCancelled: Boolean = false
  override fun throwIfCancelled() {}
}

