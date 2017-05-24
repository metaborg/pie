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
  fun <I : In, O : Out> require(request: BuildRequest<I, O>, stamper: OutputStamper = EqualsOutputStamper.instance): O
  fun require(path: CPath, stamper: PathStamper = ModifiedPathStamper.instance)
  fun generate(path: CPath, stamper: PathStamper = HashPathStamper.instance)
}


class LambdaBuilder<in I : In, out O : Out>(override val id: String, val descFunc: (I) -> String, val buildFunc: BuildContext.(I) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return descFunc(input)
  }

  override fun build(input: I, context: BuildContext): O {
    return context.buildFunc(input)
  }
}

