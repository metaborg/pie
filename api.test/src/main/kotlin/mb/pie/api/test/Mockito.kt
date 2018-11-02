package mb.pie.api.test

import mb.fs.api.node.FSNode
import mb.fs.api.path.FSPath
import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.api.stamp.OutputStamper
import mb.pie.api.stamp.ResourceStamper
import org.mockito.Mockito

inline fun <reified T : Any> safeAny(default: T) = Mockito.any(T::class.java) ?: default


class NoExecReason : ExecReason {
  override fun toString() = ""
}

fun anyER() = safeAny<ExecReason>(NoExecReason())


class NoExecContext : ExecContext {
  override fun <I : In, O : Out> require(task: Task<I, O>): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : In, O : Out> require(task: Task<I, O>, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : In, O : Out> require(taskDef: TaskDef<I, O>, input: I, stamper: OutputStamper): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun <I : In> require(task: STask<I>): Out {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun <I : In> require(task: STask<I>, stamper: OutputStamper): Out {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun <I : In> require(taskDefId: String, input: I): Out {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun <I : In> require(taskDefId: String, input: I, stamper: OutputStamper): Out {
    @Suppress("UNCHECKED_CAST")
    return null
  }

  override fun require(resource: Resource) {}
  override fun require(resource: Resource, stamper: ResourceStamper) {}
  override fun require(path: FSPath): FSNode {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as FSNode
  }

  override fun require(path: FSPath, stamper: ResourceStamper): FSNode {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as FSNode
  }

  override fun provide(resource: Resource) {}
  override fun provide(resource: Resource, stamper: ResourceStamper) {}
  override fun provide(path: FSPath) {}
  override fun provide(path: FSPath, stamper: ResourceStamper) {}

  override fun toNode(path: FSPath): FSNode {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return null as FSNode
  }


  override val logger: Logger = null!!
}

fun anyEC() = safeAny<ExecContext>(NoExecContext())

fun anyC() = safeAny<Cancelled>(NullCancelled())
