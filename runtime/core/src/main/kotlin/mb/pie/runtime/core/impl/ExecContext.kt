package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.OutputStamper
import mb.pie.runtime.core.stamp.PathStamper
import mb.util.async.Cancelled
import mb.vfs.path.PPath


internal class ExecContextImpl(
  private val exec: Exec,
  private val store: Store,
  private val currentApp: UFuncApp, // TODO: remove
  private val generatorOfLocal: MutableMap<PPath, UFuncApp>, // TODO: remove
  private val cancel: Cancelled
) : ExecContext {
  private val callReqs = arrayListOf<CallReq>()
  private val pathReqs = arrayListOf<PathReq>()
  private val pathGens = arrayListOf<PathGen>()
//  private val calls = mutableSetOf<UFuncApp>()
//  private val requiredPaths = mutableSetOf<>()
//  private val reqs = mutableListOf<Req>()
//  private val pathReqsToWrite = mutableListOf<PathReq>()
//  private val gens = mutableListOf<PathGen>()
//  private val gensToWrite = mutableListOf<PathGen>()


  override fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, stamper: OutputStamper): O {
    cancel.throwIfCancelled()
//    store.writeTxn().use {
//      it.setCallerOf(currentApp, app)
//      writePathDepsToStore(it)
//    }
    val output = exec.require(app, cancel).output
    val stamp = stamper.stamp(output)
    callReqs.add(CallReq(app, stamp))
    return output
  }

  override fun requireExec(app: UFuncApp, stamper: OutputStamper) {
    cancel.throwIfCancelled()
//    store.writeTxn().use {
//      it.setCallerOf(currentApp, app)
//      writePathDepsToStore(it)
//    }
    val output = exec.require(app, cancel).output
    val stamp = stamper.stamp(output)
    callReqs.add(CallReq(app, stamp))
  }


  override fun require(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    pathReqs.add(PathReq(path, stamp))
//    reqs.add(req)
//    pathReqsToWrite.add(req)

    val generatedBy = /*generatorOfLocal[path] ?:*/ store.readTxn().use { it.generatorOf(path) }
    if(generatedBy != null) {
      requireExec(generatedBy)
    }
  }

  override fun generate(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    pathGens.add(PathGen(path, stamp))
    //generatorOfLocal[path] = currentApp
//    gens.add(gen)
//    gensToWrite.add(gen)
  }

  data class Reqs(val callReqs: ArrayList<CallReq>, val pathReqs: ArrayList<PathReq>, val pathGens: ArrayList<PathGen>)

  fun reqs() = Reqs(callReqs, pathReqs, pathGens)

//  override fun require(req: Req) {
//    reqs.add(req)
//  }
//
//
//  fun writePathDepsToStore(txn: StoreWriteTxn) {
//    for((path, _) in pathReqsToWrite) {
//      txn.setRequireeOf(currentApp, path)
//    }
//    for((path, _) in gensToWrite) {
//      txn.setGeneratorOf(currentApp, path)
//    }
//    pathReqsToWrite.clear()
//    gensToWrite.clear()
//  }
//
//  data class ReqsAndGens(val reqs: List<Req>, val pathGens: List<PathGen>)
//
//  fun getReqsAndGens(): ReqsAndGens {
//    return ReqsAndGens(reqs, gens)
//  }
}