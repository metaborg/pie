package mb.pie.runtime.core

import mb.pie.runtime.core.impl.Req
import mb.vfs.path.PPath
import kotlin.reflect.KClass


interface ExecContext {
  @Throws(ExecException::class)
  fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, stamper: OutputStamper = OutputStampers.equals): O

  @Throws(ExecException::class)
  fun requireExec(app: UFuncApp, stamper: OutputStamper = OutputStampers.inconsequential)

  fun require(path: PPath, stamper: PathStamper = PathStampers.modified)
  fun generate(path: PPath, stamper: PathStamper = PathStampers.hash)

  fun require(req: Req)
}

@Throws(ExecException::class)
fun <I : In, O : Out, F : Func<I, O>> ExecContext.requireOutput(@Suppress("UNUSED_PARAMETER") clazz: KClass<F>, id: String, input: I, stamper: OutputStamper = OutputStampers.equals): O {
  return requireOutput(FuncApp<I, O>(id, input), stamper)
}

@Throws(ExecException::class)
fun <I : In, O : Out, F : Func<I, O>> ExecContext.requireExec(@Suppress("UNUSED_PARAMETER") clazz: KClass<F>, id: String, input: I, stamper: OutputStamper = OutputStampers.inconsequential) {
  requireExec(FuncApp<I, O>(id, input), stamper)
}


class ExecException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}
