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
  override fun <I : Serializable, O : Serializable?> require(task: Task<I, O>): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : Serializable, O : Serializable?> require(task: Task<I, O>, stamper: OutputStamper): O {
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

  override fun <I : Serializable> require(task: STask<I>): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun <I : Serializable> require(task: STask<I>, stamper: OutputStamper): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun <I : Serializable> require(taskDefId: String, input: I): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun <I : Serializable> require(taskDefId: String, input: I, stamper: OutputStamper): Serializable? {
    @Suppress("UNCHECKED_CAST")
    return null
  }


  override fun <R : Resource> require(resource: R, stamper: ResourceStamper<R>) {}
  override fun <R : Resource> provide(resource: R, stamper: ResourceStamper<R>) {}
  override fun require(path: FSPath) = null!!
  override fun require(path: FSPath, stamper: ResourceStamper<FSResource>) = null!!
  override fun defaultRequireFileSystemStamper(): ResourceStamper<FSResource> = null!!
  override fun provide(path: FSPath) {}
  override fun provide(path: FSPath, stamper: ResourceStamper<FSResource>) {}
  override fun defaultProvideFileSystemStamper(): ResourceStamper<FSResource> = null!!


  override fun getResource(key: ResourceKey): Resource {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as Resource
  }


  override fun logger(): Logger = null!!
}

fun anyEC() = safeAny<ExecContext>(NoExecContext())

fun anyC() = safeAny<Cancelled>(NullCancelled())
