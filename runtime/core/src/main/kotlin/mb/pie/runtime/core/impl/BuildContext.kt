package mb.pie.runtime.core.impl

import com.google.inject.Injector
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.store.BuildStore
import mb.pie.runtime.core.impl.store.BuildStoreWriteTxn
import mb.vfs.path.PPath

internal class BuildContextImpl(
  private val build: Build,
  private val store: BuildStore,
  private val injector: Injector,
  private val currentApp: UBuildApp)
  : BuildContext {
  private var readTxn = store.readTxn()
  private val reqs = mutableListOf<Req>()
  private val pathReqsToWrite = mutableListOf<PathReq>()
  private val gens = mutableListOf<Gen>()
  private val gensToWrite = mutableListOf<Gen>()


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
    readTxn.close()
    store.writeTxn().use { writePathDepsToStore(it) }
    val result = build.require(app).result
    val stamp = stamper.stamp(result.output)
    val req = BuildReq(app, stamp)
    reqs.add(req)
    readTxn = store.readTxn()
    return result.output
  }

  override fun requireBuild(app: UBuildApp, stamper: OutputStamper) {
    readTxn.close()
    store.writeTxn().use { writePathDepsToStore(it) }
    val result = build.require(app).result
    val stamp = stamper.stamp(result.output)
    val req = BuildReq(app, stamp)
    reqs.add(req)
    readTxn = store.readTxn()
  }


  override fun require(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    val req = PathReq(path, stamp)
    reqs.add(req)
    pathReqsToWrite.add(req)

    val generatedBy = readTxn.generatedBy(path)
    if (generatedBy != null) {
      requireBuild(generatedBy)
    }
  }

  override fun generate(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    val gen = Gen(path, stamp)
    gens.add(gen)
    gensToWrite.add(gen)
  }


  override fun close() {
    readTxn.close()
  }


  fun writePathDepsToStore(txn: BuildStoreWriteTxn) {
    for ((path, _) in pathReqsToWrite) {
      txn.setRequiredBy(path, currentApp)
    }
    for ((path, _) in gensToWrite) {
      txn.setGeneratedBy(path, currentApp)
    }
    pathReqsToWrite.clear()
    gensToWrite.clear()
  }

  data class ReqsAndGens(val reqs: List<Req>, val gens: List<Gen>)

  fun getReqsAndGens(): ReqsAndGens {
    return ReqsAndGens(reqs, gens)
  }
}