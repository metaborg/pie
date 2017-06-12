package mb.ceres

import java.io.Serializable

typealias In = Serializable?
typealias Out = Serializable?

data class OutTransient<T : Any?>(@Transient val v: T, @Transient val consistent: Boolean = false) : Out {
  constructor(v: T) : this(v, true)
}


interface Builder<in I : In, out O : Out> {
  @Throws(BuildException::class)
  fun BuildContext.build(input: I): O

  @Throws(BuildException::class)
  fun build(input: I, ctx: BuildContext): O {
    return ctx.build(input)
  }

  fun mayOverlap(input1: I, input2: I): Boolean = input1 == input2

  val id: String
  fun desc(input: I): String = "$id($input)"
}
typealias UBuilder = Builder<*, *>


interface OutEffectBuilder<in I : In> : Builder<I, None> {
  @Throws(BuildException::class)
  fun BuildContext.effect(input: I)

  @Throws(BuildException::class)
  override fun BuildContext.build(input: I): None {
    this.effect(input)
    return None.instance
  }
}

interface InEffectBuilder<out O : Out> : Builder<None, O> {
  @Throws(BuildException::class)
  fun BuildContext.effect(): O

  @Throws(BuildException::class)
  override fun BuildContext.build(input: None): O {
    return this.effect()
  }

  override fun mayOverlap(input1: None, input2: None) = true
}

interface EffectBuilder : Builder<None, None> {
  @Throws(BuildException::class)
  fun BuildContext.effect()

  @Throws(BuildException::class)
  override fun BuildContext.build(input: None): None {
    effect()
    return None.instance
  }

  override fun mayOverlap(input1: None, input2: None) = true
}

class BuildException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}

interface BuildContext {
  @Throws(BuildException::class)
  fun <I : In, O : Out> requireOutput(app: BuildApp<I, O>, stamper: OutputStamper = OutputStampers.equals): O

  @Throws(BuildException::class)
  fun requireBuild(app: UBuildApp, stamper: OutputStamper = OutputStampers.equals)

  @Throws(BuildException::class)
  fun <I : In, O : Out, B : Builder<I, O>> requireOutput(clazz: Class<B>, input: I, stamper: OutputStamper = OutputStampers.equals): O

  @Throws(BuildException::class)
  fun <I : In, B : Builder<I, *>> requireBuild(clazz: Class<B>, input: I, stamper: OutputStamper = OutputStampers.equals)

  fun require(path: CPath, stamper: PathStamper = PathStampers.modified)
  fun generate(path: CPath, stamper: PathStamper = PathStampers.hash)
}

@Throws(BuildException::class)
inline fun <I : In, O : Out, reified B : Builder<I, O>> BuildContext.requireOutput(input: I, stamper: OutputStamper = OutputStampers.equals): O {
  return this.requireOutput(B::class.java, input, stamper)
}

@Throws(BuildException::class)
inline fun <I : In, reified B : Builder<I, *>> BuildContext.requireBuild(input: I, stamper: OutputStamper = OutputStampers.equals) {
  return this.requireBuild(B::class.java, input, stamper)
}


data class BuildApp<out I : In, out O : Out>(val builderId: String, val input: I) : Serializable {
  constructor(builder: Builder<I, O>, input: I) : this(builder.id, input)
}
typealias UBuildApp = BuildApp<*, *>


open class LambdaBuilder<in I : In, out O : Out>(override val id: String, val descFunc: (I) -> String, val buildFunc: BuildContext.(I) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return descFunc(input)
  }

  override fun BuildContext.build(input: I): O {
    return buildFunc(input)
  }
}
