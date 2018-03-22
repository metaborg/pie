package mb.pie.runtime.core

import java.io.Serializable


typealias Out = Serializable?

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <O : Out> Out.cast() = this as O


interface OutTransient<out T : Any?> : Out {
  val v: T
  val consistent: Boolean
}

data class OutTransientImpl<out T : Any?>(
  @Transient override val v: T,
  @Transient override val consistent: Boolean = false
) : OutTransient<T> {
  constructor(v: T) : this(v, true)
}


interface OutTransientEquatable<out T : Any?, out E : Out> : OutTransient<T> {
  val e: E
}

data class OutTransientEquatableImpl<out T : Any?, out E : Out>(
  @Transient override val v: T,
  @Transient override val consistent: Boolean = false,
  override val e: E
) : OutTransientEquatable<T, E> {
  constructor(v: T, e: E) : this(v, true, e)
}


/**
 * @return an [execution reason][ExecReason] when this output is transient and not consistent, `null` otherwise.
 */
internal fun Out.isTransientInconsistent(): ExecReason? {
  return when(this) {
    is OutTransient<*> -> when {
      this.consistent -> null
      else -> InconsistentTransientOutput(this)
    }
    else -> null
  }
}
