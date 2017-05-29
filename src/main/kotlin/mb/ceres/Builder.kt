package mb.ceres

import java.io.Serializable

typealias In = Serializable
typealias Out = Serializable

interface Builder<in I : In, out O : Out> {
  val id: String

  fun desc(input: I): String
  fun build(input: I, context: BuildContext): O
}
typealias UBuilder = Builder<*, *>

interface BuildContext {
  fun <I : In, O : Out> requireOutput(app: BuildApp<I, O>, stamper: OutputStamper = OutputStampers.equals): O
  fun requireBuild(app: UBuildApp, stamper: OutputStamper = OutputStampers.equals)

  fun require(path: CPath, stamper: PathStamper = PathStampers.modified)
  fun generate(path: CPath, stamper: PathStamper = PathStampers.hash)
}

data class BuildApp<out I : In, out O : Out>(val builderId: String, val input: I) : Serializable {
  constructor(builder: Builder<I, O>, input: I) : this(builder.id, input)
}
typealias UBuildApp = BuildApp<*, *>

open class LambdaBuilder<in I : In, out O : Out>(override val id: String, val descFunc: (I) -> String, val buildFunc: BuildContext.(I) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return descFunc(input)
  }

  override fun build(input: I, context: BuildContext): O {
    return context.buildFunc(input)
  }
}
