package mb.pie.runtime.core.impl

import com.google.inject.Injector
import mb.pie.runtime.core.*
import mb.vfs.path.PPath


internal class ExecContextImpl(
  private val exec: Exec,
  private val store: Store,
  private val injector: Injector,
  private val currentApp: UFuncApp)
  : ExecContext {
  private val reqs = mutableListOf<Req>()
  private val pathReqsToWrite = mutableListOf<PathReq>()
  private val gens = mutableListOf<Gen>()
  private val gensToWrite = mutableListOf<Gen>()


  override fun <I : In, O : Out, B : Func<I, O>> requireOutput(clazz: Class<B>, input: I, stamper: OutputStamper): O {
    val builder = injector.getInstance(clazz)
    val app = FuncApp(builder, input)
    return requireOutput(app, stamper)
  }

  override fun <I : In, B : Func<I, *>> requireExec(clazz: Class<B>, input: I, stamper: OutputStamper) {
    val builder = injector.getInstance(clazz)
    val app = FuncApp(builder, input)
    requireExec(app, stamper)
  }


  override fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, stamper: OutputStamper): O {
    store.writeTxn().use { writePathDepsToStore(it) }
    val result = exec.require(app).result
    val stamp = stamper.stamp(result.output)
    val req = ExecReq(app, stamp)
    reqs.add(req)
    return result.output
  }

  override fun requireExec(app: UFuncApp, stamper: OutputStamper) {
    store.writeTxn().use { writePathDepsToStore(it) }
    val result = exec.require(app).result
    val stamp = stamper.stamp(result.output)
    val req = ExecReq(app, stamp)
    reqs.add(req)
  }


  override fun require(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    val req = PathReq(path, stamp)
    reqs.add(req)
    pathReqsToWrite.add(req)

    val generatedBy = store.readTxn().use { it.generatedBy(path) }
    if(generatedBy != null) {
      requireExec(generatedBy)
    }
  }

  override fun generate(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    val gen = Gen(path, stamp)
    gens.add(gen)
    gensToWrite.add(gen)
  }


  fun writePathDepsToStore(txn: StoreWriteTxn) {
    for((path, _) in pathReqsToWrite) {
      txn.setRequiredBy(path, currentApp)
    }
    for((path, _) in gensToWrite) {
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