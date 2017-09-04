package mb.ceres

open class LambdaBuilder<in I : In, out O : Out>(override val id: String, val descFunc: (I) -> String, val buildFunc: BuildContext.(I) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return descFunc(input)
  }

  override fun BuildContext.build(input: I): O {
    return buildFunc(input)
  }
}
