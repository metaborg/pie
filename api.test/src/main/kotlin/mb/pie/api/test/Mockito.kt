package mb.pie.api.test

import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import mb.resource.Resource
import mb.resource.ResourceKey
import mb.resource.fs.FSPath
import mb.resource.fs.FSResource
import org.mockito.Mockito
import java.io.Serializable

inline fun <reified T : Any> safeAny(default: T) = Mockito.any(T::class.java) ?: default


class NoExecReason : ExecReason {
  override fun toString() = ""
}

fun anyER() = safeAny<ExecReason>(NoExecReason())


class NoExecContext : ExecContext {
  override fun <O : Serializable?> require(task: Task<O>): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <O : Serializable?> require(task: Task<O>, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : Serializable, O : Serializable?> require(taskDef: TaskDef<I, O>, input: I): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : Serializable, O : Serializable?> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun require(task: STask): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun require(task: STask, stamper: OutputStamper): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun require(taskDefId: String, input: Serializable): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun require(taskDefId: String, input: Serializable, stamper: OutputStamper): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun defaultOutputStamper() = null!!


  override fun <R : Resource> require(resource: R, stamper: ResourceStamper<R>) {}
  override fun <R : Resource> provide(resource: R, stamper: ResourceStamper<R>) {}
  override fun getResource(key: ResourceKey): Resource {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as Resource
  }

  override fun defaultRequireReadableResourceStamper() = null!!
  override fun defaultProvideReadableResourceStamper() = null!!

  override fun require(path: FSPath) = null!!
  override fun require(path: FSPath, stamper: ResourceStamper<FSResource>) = null!!
  override fun defaultRequireFSResourceStamper(): ResourceStamper<FSResource> = null!!

  override fun provide(path: FSPath) {}
  override fun provide(path: FSPath, stamper: ResourceStamper<FSResource>) {}
  override fun defaultProvideFSResourceStamper(): ResourceStamper<FSResource> = null!!

  override fun logger(): Logger = null!!
}

fun anyEC() = safeAny<ExecContext>(NoExecContext())

fun anyC() = safeAny<Cancelled>(NullCancelled())
