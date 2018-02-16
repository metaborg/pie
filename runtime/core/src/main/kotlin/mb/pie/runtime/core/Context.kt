package mb.pie.runtime.core

import mb.pie.runtime.core.stamp.*
import mb.vfs.path.PPath
import kotlin.reflect.KClass


interface ExecContext {
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, stamper: OutputStamper = OutputStampers.equals): O

  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : Func<I, O>> requireOutput(@Suppress("UNUSED_PARAMETER") clazz: KClass<F>, id: String, input: I, stamper: OutputStamper = OutputStampers.equals): O =
    requireOutput(FuncApp<I, O>(id, input), stamper)

  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : Func<I, O>> requireOutput(clazz: KClass<F>, input: I, stamper: OutputStamper = OutputStampers.equals): O =
    requireOutput(FuncApp<I, O>(clazz.java.canonicalName!!, input), stamper)


  @Throws(ExecException::class, InterruptedException::class)
  fun requireExec(app: UFuncApp, stamper: OutputStamper = OutputStampers.inconsequential)

  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : Func<I, O>> requireExec(@Suppress("UNUSED_PARAMETER") clazz: KClass<F>, id: String, input: I, stamper: OutputStamper = OutputStampers.inconsequential) {
    requireExec(FuncApp<I, O>(id, input), stamper)
  }

  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out, F : Func<I, O>> requireExec(clazz: KClass<F>, input: I, stamper: OutputStamper = OutputStampers.inconsequential) {
    requireExec(FuncApp<I, O>(clazz.java.canonicalName!!, input), stamper)
  }


  fun require(path: PPath, stamper: PathStamper = PathStampers.modified)
  fun generate(path: PPath, stamper: PathStamper = PathStampers.hash)
}

class ExecException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}
