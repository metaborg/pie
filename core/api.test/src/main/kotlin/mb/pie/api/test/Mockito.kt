package mb.pie.api.test

import mb.log.api.Logger
import mb.pie.api.ExecContext
import mb.pie.api.Function
import mb.pie.api.STask
import mb.pie.api.STaskDef
import mb.pie.api.Supplier
import mb.pie.api.Task
import mb.pie.api.TaskDef
import mb.pie.api.exec.CancelToken
import mb.pie.api.exec.ExecReason
import mb.pie.api.exec.NullCancelableToken
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.resource.ReadableResource
import mb.resource.Resource
import mb.resource.ResourceKey
import mb.resource.ResourceService
import mb.resource.WritableResource
import mb.resource.fs.FSPath
import mb.resource.hierarchical.HierarchicalResource
import mb.resource.hierarchical.ResourcePath
import org.mockito.Mockito
import java.io.Serializable

inline fun <reified T : Any> safeAny(default: T) = Mockito.any(T::class.java) ?: default


class NoExecReason : ExecReason {
  override fun toString() = ""
}

fun anyER() = safeAny<ExecReason>(NoExecReason())


class NoExecContext : ExecContext {
  override fun <I : Serializable, O : Serializable?> require(taskDef: TaskDef<I, O>, input: I): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : Serializable, O : Serializable?> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <O : Serializable?> require(task: Task<O>): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <O : Serializable?> require(task: Task<O>, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : Serializable, O : Serializable?> require(sTaskDef: STaskDef<I, O>, input: I): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : Serializable, O : Serializable?> require(sTaskDef: STaskDef<I, O>, input: I, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <O : Serializable?> require(task: STask<O>): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <O : Serializable?> require(task: STask<O>, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <O : Serializable?> require(supplier: Supplier<O>?): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : Serializable?, O : Serializable?> require(function: Function<I, O>?, input: I): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun getDefaultOutputStamper() = null!!


  override fun <R : Resource> require(resource: R, stamper: ResourceStamper<R>) {}
  override fun <R : Resource> provide(resource: R, stamper: ResourceStamper<R>) {}


  override fun getResourceService(): ResourceService {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as ResourceService
  }

  override fun getResource(key: ResourceKey): Resource {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as Resource
  }

  override fun getReadableResource(key: ResourceKey?): ReadableResource {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as ReadableResource
  }

  override fun getWritableResource(key: ResourceKey?): WritableResource {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as WritableResource
  }

  override fun getHierarchicalResource(path: ResourcePath?): HierarchicalResource {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as HierarchicalResource
  }


  override fun getDefaultRequireReadableResourceStamper() = null!!
  override fun getDefaultProvideReadableResourceStamper() = null!!

  override fun require(path: FSPath) = null!!
  override fun require(path: FSPath, stamper: ResourceStamper<HierarchicalResource>) = null!!


  override fun getDefaultRequireHierarchicalResourceStamper(): ResourceStamper<HierarchicalResource> = null!!

  override fun provide(path: FSPath) {}
  override fun provide(path: FSPath, stamper: ResourceStamper<HierarchicalResource>) {}

  override fun getDefaultProvideHierarchicalResourceStamper(): ResourceStamper<HierarchicalResource> = null!!


  override fun cancelToken(): CancelToken = null!!
  override fun logger(): Logger = null!!
}

fun anyEC() = safeAny<ExecContext>(NoExecContext())

fun anyC() = safeAny<CancelToken>(NullCancelableToken.instance)
