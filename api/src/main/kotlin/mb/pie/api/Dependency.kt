package mb.pie.api

import mb.pie.api.exec.ExecReason
import mb.pie.api.stamp.ResourceStamp
import mb.pie.api.stamp.OutputStamp
import java.io.Serializable

/**
 * Resource dependency that can be checked for consistency, given the collection of resource systems to resolve resource keys into resources.
 */
interface ResourceDep {
  /**
   * @return an execution reason when this resource dependency is inconsistent, `null` otherwise.
   */
  fun checkConsistency(systems: ResourceSystems): ExecReason?

  /**
   * @return `true` when this resource dependency is consistent, `false` otherwise.
   */
  fun isConsistent(systems: ResourceSystems): Boolean
}

/**
 * Resource 'requires' (reads) dependency.
 */
data class ResourceRequireDep(
  val key: ResourceKey,
  val stamp: ResourceStamp<Resource>
) : ResourceDep, Serializable {
  override fun checkConsistency(systems: ResourceSystems): InconsistentResourceRequire? {
    val system = systems.getResourceSystem(key.id)
      ?: throw RuntimeException("Cannot get resource system for resource key $key; resource system with id ${key.id} does not exist")
    val resource = system.getResource(key)
    val newStamp = stamp.stamper.stamp(resource)
    if(stamp != newStamp) {
      return InconsistentResourceRequire(this, newStamp)
    }
    return null
  }

  override fun isConsistent(systems: ResourceSystems): Boolean {
    val system = systems.getResourceSystem(key.id)
      ?: throw RuntimeException("Cannot get resource system for resource key $key; resource system with id ${key.id} does not exist")
    val resource = system.getResource(key)
    val newStamp = stamp.stamper.stamp(resource)
    return stamp == newStamp
  }

  override fun toString(): String {
    return "FileReq($key, $stamp)";
  }
}

/**
 * Execution reason for inconsistent resource requires dependency.
 */
data class InconsistentResourceRequire(val dep: ResourceRequireDep, val newStamp: ResourceStamp<*>) : ExecReason {
  override fun toString() = "inconsistent required resource ${dep.key}"
}

/**
 * Resource 'provides' (writes) dependency.
 */
data class ResourceProvideDep(
  val key: ResourceKey,
  val stamp: ResourceStamp<Resource>
) : ResourceDep, Serializable {
  override fun checkConsistency(systems: ResourceSystems): InconsistentResourceProvide? {
    val system = systems.getResourceSystem(key.id)
      ?: throw RuntimeException("Cannot get resource system for resource key $key; resource system with id ${key.id} does not exist")
    val resource = system.getResource(key)
    val newStamp = stamp.stamper.stamp(resource)
    if(stamp != newStamp) {
      return InconsistentResourceProvide(this, newStamp)
    }
    return null
  }

  override fun isConsistent(systems: ResourceSystems): Boolean {
    val system = systems.getResourceSystem(key.id)
      ?: throw RuntimeException("Cannot get resource system for resource key $key; resource system with id ${key.id} does not exist")
    val resource = system.getResource(key)
    val newStamp = stamp.stamper.stamp(resource)
    return stamp == newStamp
  }
}

/**
 * Execution reason for inconsistent resource provides dependency.
 */
data class InconsistentResourceProvide(val dep: ResourceProvideDep, val newStamp: ResourceStamp<*>) : ExecReason {
  override fun toString() = "inconsistent provided resource ${dep.key}"
}

/**
 * Task 'require' (calls) dependency.
 */
data class TaskRequireDep(
  val callee: TaskKey,
  val stamp: OutputStamp
) : Serializable {
  /**
   * @return an execution reason when this call requirement is inconsistent w.r.t. [output], `null` otherwise.
   */
  fun checkConsistency(output: Out): InconsistentTaskReq? {
    val newStamp = stamp.stamper.stamp(output)
    if(stamp != newStamp) {
      return InconsistentTaskReq(this, newStamp)
    }
    return null
  }

  /**
   * @return `true` when this call requirement is consistent w.r.t. [output], `false` otherwise.
   */
  fun isConsistent(output: Out): Boolean {
    val newStamp = stamp.stamper.stamp(output)
    return newStamp == stamp
  }

  /**
   * @return `true` when this call requirement's callee is equal to [other], `false` otherwise.
   */
  fun calleeEqual(other: TaskKey): Boolean {
    return other == callee
  }

  override fun toString(): String {
    return "TaskReq(${callee.toShortString(100)}, $stamp)";
  }
}

/**
 * Execution reason for inconsistent task requires dependency.
 */
data class InconsistentTaskReq(val dep: TaskRequireDep, val newStamp: OutputStamp) : ExecReason {
  override fun toString() = "inconsistent required task ${dep.callee.toShortString(100)}"
}
