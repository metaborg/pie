package mb.ceres.impl

import com.google.inject.Injector
import mb.ceres.BuildApp
import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.ceres.CPath
import mb.ceres.In
import mb.ceres.Out
import mb.ceres.OutputStamper
import mb.ceres.PathStamper
import mb.ceres.UBuildApp

internal class BuildContextImpl(
  val build: Build,
  val injector: Injector)
  : BuildContext {
  val reqs = mutableListOf<Req>()
  val gens = mutableListOf<Gen>()


  override fun <I : In, O : Out, B : Builder<I, O>> requireOutput(clazz: Class<B>, input: I, stamper: OutputStamper): O {
    val builder = injector.getInstance(clazz)
    val app = BuildApp(builder, input)
    return requireOutput(app, stamper)
  }

  override fun <I : In, B : Builder<I, *>> requireBuild(clazz: Class<B>, input: I, stamper: OutputStamper) {
    val builder = injector.getInstance(clazz)
    val app = BuildApp(builder, input)
    requireBuild(app, stamper)
  }


  override fun <I : In, O : Out> requireOutput(app: BuildApp<I, O>, stamper: OutputStamper): O {
    val result = build.require(app).result
    val stamp = stamper.stamp(result.output)
    reqs.add(BuildReq(app, stamp))
    return result.output
  }

  override fun requireBuild(app: UBuildApp, stamper: OutputStamper) {
    val result = build.require(app).result
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
