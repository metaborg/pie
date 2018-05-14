package mb.pie.runtime.core


interface Func<in I : In, out O : Out> {
  @Throws(ExecException::class, InterruptedException::class)
  fun ExecContext.exec(input: I): O

  @Throws(ExecException::class, InterruptedException::class)
  fun exec(input: I, ctx: ExecContext): O = ctx.exec(input)

  val id: String get() = this::class.java.canonicalName!!
  fun desc(input: I): String = "$id(${input.toString().toShortString(100)})"
}

typealias UFunc = Func<*, *>
typealias AnyFunc = Func<In, Out>

@Throws(ExecException::class, InterruptedException::class)
internal fun <I : In> Func<I, *>.execUntyped(input: In, ctx: ExecContext): Out = exec(input.cast(), ctx)


interface OutEffectFunc<in I : In> : Func<I, None> {
  @Throws(ExecException::class, InterruptedException::class)
  fun ExecContext.effect(input: I)

  @Throws(ExecException::class, InterruptedException::class)
  override fun ExecContext.exec(input: I): None {
    this.effect(input)
    return None.instance
  }
}

interface InEffectFunc<out O : Out> : Func<None, O> {
  @Throws(ExecException::class, InterruptedException::class)
  fun ExecContext.effect(): O

  @Throws(ExecException::class, InterruptedException::class)
  override fun ExecContext.exec(input: None): O = this.effect()
}

interface EffectFunc : Func<None, None> {
  @Throws(ExecException::class, InterruptedException::class)
  fun ExecContext.effect()

  @Throws(ExecException::class, InterruptedException::class)
  override fun ExecContext.exec(input: None): None {
    effect()
    return None.instance
  }
}


open class LambdaFunc<in I : In, out O : Out>(override val id: String, private val execFunc: ExecContext.(I) -> O) : Func<I, O> {
  override fun ExecContext.exec(input: I): O = execFunc(input)
}

open class LambdaFuncD<in I : In, out O : Out>(override val id: String, private val descFunc: (I) -> String, private val execFunc: ExecContext.(I) -> O) : Func<I, O> {
  override fun desc(input: I): String = descFunc(input)

  override fun ExecContext.exec(input: I): O = execFunc(input)
}
