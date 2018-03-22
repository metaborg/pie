package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.OutputStamper
import mb.pie.runtime.core.stamp.PathStamper
import mb.util.async.Cancelled
import mb.vfs.path.PPath


internal class ExecContextImpl(
  private val exec: Exec,
  private val store: Store,
  private val cancel: Cancelled
) : ExecContext {
  private val callReqs = arrayListOf<CallReq>()
  private val pathReqs = arrayListOf<PathReq>()
  private val pathGens = arrayListOf<PathGen>()


  override fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, stamper: OutputStamper): O {
    cancel.throwIfCancelled()
    val output = exec.require(app, cancel).output
    val stamp = stamper.stamp(output)
    callReqs.add(CallReq(app, stamp))
    return output
  }

  override fun requireExec(app: UFuncApp, stamper: OutputStamper) {
    cancel.throwIfCancelled()
    val output = exec.require(app, cancel).output
    val stamp = stamper.stamp(output)
    callReqs.add(CallReq(app, stamp))
  }


  override fun require(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    pathReqs.add(PathReq(path, stamp))

    val generatedBy = store.readTxn().use { it.generatorOf(path) }
    if(generatedBy != null) {
      requireExec(generatedBy)
    }
  }

  override fun generate(path: PPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    pathGens.add(PathGen(path, stamp))
  }

  data class Reqs(val callReqs: ArrayList<CallReq>, val pathReqs: ArrayList<PathReq>, val pathGens: ArrayList<PathGen>)

  fun reqs() = Reqs(callReqs, pathReqs, pathGens)
}