package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason

/**
 * [Execution reason][ExecReason] for when there is no data of a task.
 */
class NoData : ExecReason {
  override fun toString() = "no stored or cached data"

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

data class InconsistentInput(val oldInput: In, val newInput: In) : ExecReason {
  override fun toString() = "inconsistent input"
}

/**
 * [Execution reason][ExecReason] for when the transient output of a task is inconsistent.
 */
data class InconsistentTransientOutput(val inconsistentOutput: OutTransient<*>) : ExecReason {
  override fun toString() = "inconsistent transient output"
}

/**
 * [Execution reason][ExecReason] for when the transient output of a task is inconsistent.
 */
data class UnobservedRequired(val observability: Observability) : ExecReason {
  override fun toString() = "observed but output was required"
}


/**
 * @return an [execution reason][ExecReason] when this output is transient and not consistent, `null` otherwise.
 */
fun Out.isTransientInconsistent(): InconsistentTransientOutput? {
  return when(this) {
    is OutTransient<*> -> when {
      this.consistent -> null
      else -> InconsistentTransientOutput(this)
    }
    else -> null
  }
}
