package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason

/**
 * [Execution reason][ExecReason] for when there is no data of a task.
 */
public class NoData : ExecReason {
  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other != null && other.javaClass != javaClass) return false;
    return true;
  }

  override fun hashCode(): Int {
    return 0;
  }

  override fun toString(): String {
    return "no stored or cached data";
  }
}

public class InconsistentInput : ExecReason {
  public val oldInput: In;
  public val newInput: In;

  public constructor(oldInput: In, newInput: In) {
    this.oldInput = oldInput;
    this.newInput = newInput;
  }

  // TODO: generate equals and hashcode.

  override fun toString(): String {
    return "inconsistent input";
  }
}

/**
 * [Execution reason][ExecReason] for when the transient output of a task is inconsistent.
 */
public class InconsistentTransientOutput : ExecReason {
  public val inconsistentOutput: OutTransient<*>;

  public constructor(inconsistentOutput: OutTransient<*>) {
    this.inconsistentOutput = inconsistentOutput;
  }

  // TODO: generate equals and hashcode.

  override fun toString(): String {
    return "inconsistent transient output";
  }
}

/**
 * @return an [execution reason][ExecReason] when this output is transient and not consistent, `null` otherwise.
 */
fun isTransientInconsistent(output: Out): InconsistentTransientOutput? {
  if(output is OutTransient<*>) {
    if(output.isConsistent) {
      return null;
    } else {
      return InconsistentTransientOutput(output);
    }
  } else {
    return null;
  }
}
