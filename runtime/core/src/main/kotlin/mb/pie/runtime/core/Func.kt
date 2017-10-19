package mb.pie.runtime.core


interface Func<in I : In, out O : Out> {
  @Throws(ExecException::class)
  fun ExecContext.exec(input: I): O

  @Throws(ExecException::class)
  fun exec(input: I, ctx: ExecContext): O {
    return ctx.exec(input)
  }

  fun mayOverlap(input1: I, input2: I): Boolean = input1 == input2

  val id: String
  fun desc(input: I): String = "$id(${input.toString().toShortString(100)})"
}

typealias UFunc = Func<*, *>
typealias AnyFunc = Func<In, Out>


interface OutEffectFunc<in I : In> : Func<I, None> {
  @Throws(ExecException::class)
  fun ExecContext.effect(input: I)

  @Throws(ExecException::class)
  override fun ExecContext.exec(input: I): None {
    this.effect(input)
    return None.instance
  }
}

interface InEffectFunc<out O : Out> : Func<None, O> {
  @Throws(ExecException::class)
  fun ExecContext.effect(): O

  @Throws(ExecException::class)
  override fun ExecContext.exec(input: None): O {
    return this.effect()
  }

  override fun mayOverlap(input1: None, input2: None) = true
}

interface EffectFunc : Func<None, None> {
  @Throws(ExecException::class)
  fun ExecContext.effect()

  @Throws(ExecException::class)
  override fun ExecContext.exec(input: None): None {
    effect()
    return None.instance
  }

  override fun mayOverlap(input1: None, input2: None) = true
}


open class LambdaFunc<in I : In, out O : Out>(override val id: String, private val descFunc: (I) -> String, private val execFunc: ExecContext.(I) -> O) : Func<I, O> {
  override fun desc(input: I): String {
    return descFunc(input)
  }

  override fun ExecContext.exec(input: I): O {
    return execFunc(input)
  }
}
