package mb.pie.runtime.core

import mb.vfs.path.PPath


interface ExecContext {
  @Throws(ExecException::class)
  fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, stamper: OutputStamper = OutputStampers.equals): O

  @Throws(ExecException::class)
  fun requireExec(app: UFuncApp, stamper: OutputStamper = OutputStampers.inconsequential)

  @Throws(ExecException::class)
  fun <I : In, O : Out, B : Func<I, O>> requireOutput(clazz: Class<B>, input: I, stamper: OutputStamper = OutputStampers.equals): O

  @Throws(ExecException::class)
  fun <I : In, B : Func<I, *>> requireExec(clazz: Class<B>, input: I, stamper: OutputStamper = OutputStampers.inconsequential)

  fun require(path: PPath, stamper: PathStamper = PathStampers.modified)
  fun generate(path: PPath, stamper: PathStamper = PathStampers.hash)
}

@Throws(ExecException::class)
inline fun <I : In, O : Out, reified B : Func<I, O>> ExecContext.requireOutput(input: I, stamper: OutputStamper = OutputStampers.equals): O {
  return this.requireOutput(B::class.java, input, stamper)
}

@Throws(ExecException::class)
inline fun <I : In, reified B : Func<I, *>> ExecContext.requireExec(input: I, stamper: OutputStamper = OutputStampers.inconsequential) {
  return this.requireExec(B::class.java, input, stamper)
}


class ExecException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}
