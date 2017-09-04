package mb.ceres

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