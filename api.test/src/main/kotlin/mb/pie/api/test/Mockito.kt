package mb.pie.api.test

import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.vfs.path.PPath
import org.mockito.Mockito

inline fun <reified T : Any> safeAny(default: T) = Mockito.any(T::class.java) ?: default


class NoExecReason : ExecReason {
  override fun toString() = ""
}

fun anyER() = safeAny<ExecReason>(NoExecReason())


class NoExecContext : ExecContext {
  override fun <I : In, O : Out> requireOutput(task: Task<I, O>, stamper: OutputStamper?): O {
    @Suppress("UNCHECKED_CAST")
    return null as O
  }

  override fun requireExec(task: UTask) {}
  override fun require(file: PPath, stamper: FileStamper?) {}
  override fun generate(file: PPath, stamper: FileStamper?) {}
  override val logger: Logger = null!!
}

fun anyEC() = safeAny<ExecContext>(NoExecContext())


fun anyC() = safeAny<Cancelled>(NullCancelled())
