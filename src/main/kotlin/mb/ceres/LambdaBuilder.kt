package mb.ceres

class SimpleLambdaBuilder<in I : In, out O : Out>(override val id: String, val desc: String, val buildFunc: (I) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return desc
  }

  override fun build(input: I, context: BuildContext): O? {
    return buildFunc(input)
  }
}

class LambdaBuilder<in I : In, out O : Out>(override val id: String, val descFunc: (I) -> String, val buildFunc: (input: I, buildContext: BuildContext) -> O) : Builder<I, O> {
  override fun desc(input: I): String {
    return descFunc(input)
  }

  override fun build(input: I, context: BuildContext): O? {
    return buildFunc(input, context)
  }
}

