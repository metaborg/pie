package mb.ceres

class SimpleLambdaBuilder<in I, out O>(val name: String, val path: CPath, val build: (I) -> O) : Builder<I, O> {
  override fun name(input: I): String {
    return name
  }

  override fun path(input: I): CPath {
    return path
  }

  override fun build(input: I, context: BuildContext): O? {
    return build(input)
  }
}

class LambdaBuilder<in I, out O>(val name: (I) -> String, val path: (I) -> CPath, val build: (input: I, buildContext: BuildContext) -> O) : Builder<I, O> {
  override fun name(input: I): String {
    return name(input)
  }

  override fun path(input: I): CPath {
    return path(input)
  }

  override fun build(input: I, context: BuildContext): O? {
    return build(input, context)
  }
}

