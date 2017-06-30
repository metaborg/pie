package mb.ceres

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
