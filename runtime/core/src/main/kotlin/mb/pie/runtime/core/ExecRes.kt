package mb.pie.runtime.core

import mb.pie.runtime.core.impl.*
import java.io.Serializable


data class ExecRes<out I : In, out O : Out>(val builderId: String, val desc: String, val input: I, val output: O, val reqs: List<Req>, val gens: List<Gen>) : Serializable {
  val toApp get() = mb.pie.runtime.core.FuncApp<I, O>(builderId, input)

  val inconsistencyReason: ExecReason?
    get() {
      if(output is OutTransient<*>) {
        return if(output.consistent) null else InconsistentTransientOutput(this)
      }
      return null
    }
  val isConsistent: Boolean get() = inconsistencyReason == null

  fun requires(other: UFuncApp, funcs: Funcs): Boolean {
    // We require other when we have a build requirement with the same builder and input as other, or when their inputs overlap.
    for((req, _) in reqs.filterIsInstance<UExecReq>().filter { it.app.builderId == other.builderId }) {
      if(req == other) {
        return true
      }
      val builder = funcs.getAnyFunc(other.builderId);
      if(builder.mayOverlap(req.input, other.input)) {
        return true
      }
    }
    return false
  }

  fun toShortString(maxLength: Int) = output.toString().toShortString(maxLength)
}

typealias UExecRes = ExecRes<*, *>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I : In, O : Out> UExecRes.cast() = this as ExecRes<I, O>
