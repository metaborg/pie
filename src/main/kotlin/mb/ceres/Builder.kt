package mb.ceres

import java.io.Serializable

typealias In = Serializable
typealias Out = Serializable

interface Builder<in I : In, out O : Out> {
  val id: String

  fun desc(input: I): String
  fun build(input: I, context: BuildContext): O
}

interface BuildContext {
  fun <I : In, O : Out> require(request: BuildRequest<I, O>, stamper: OutputStamper = EqualsOutputStamper()): O
  fun require(path: CPath, stamper: PathStamper = ModifiedPathStamper())
  fun generate(path: CPath, stamper: PathStamper = HashPathStamper())
}


class SimpleLambdaBuilder<in I : In, out O : Out>(override val id: String, val desc: String, val buildFunc: (I) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return desc
  }

  override fun build(input: I, context: BuildContext): O {
    return buildFunc(input)
  }
}

class LambdaBuilder<in I : In, out O : Out>(override val id: String, val descFunc: (I) -> String, val buildFunc: (input: I, buildContext: BuildContext) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return descFunc(input)
  }

  override fun build(input: I, context: BuildContext): O {
    return buildFunc(input, context)
  }
}

