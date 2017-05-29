package mb.ceres.internal

import mb.ceres.*

internal class BuildContextImpl(val buildManager: BuildManagerInternal) : BuildContext {
  val reqs = mutableListOf<Req>()
  val gens = mutableListOf<Gen>()

  override fun <I : In, O : Out> requireOutput(app: BuildApp<I, O>, stamper: OutputStamper): O {
    val result = buildManager.require(app)
    val stamp = stamper.stamp(result.output)
    reqs.add(BuildReq(app, stamp))
    return result.output
  }

  override fun requireBuild(app: UBuildApp, stamper: OutputStamper) {
    val result = buildManager.require(app)
    val stamp = stamper.stamp(result.output)
    reqs.add(BuildReq(app, stamp))
  }

  override fun require(path: CPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    reqs.add(PathReq(path, stamp))
  }

  override fun generate(path: CPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    gens.add(Gen(path, stamp))
  }
}
