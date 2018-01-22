package mb.pie.runtime.core

import mb.pie.runtime.core.impl.*
import mb.vfs.path.PPath
import java.io.Serializable


data class ExecRes<out I : In, out O : Out>(val id: String, val desc: String, val input: I, val output: O, val reqs: List<Req>, val gens: List<Gen>) : Serializable {
  /**
   * @return all [path requirements][PathReq] of this result.
   */
  val pathReqs get() = reqs.filterIsInstance<PathReq>()

  /**
   * @return [path requirements][PathReq] that require [path] of this result.
   */
  fun pathReq(path: PPath) = pathReqs.filter { it.path == path }

  /**
   * @return `true` when this result requires [path], `false` otherwise.
   */
  fun requires(path: PPath) = pathReqs.any { it.path == path }


  /**
   * @return all [call requirements][UCallReq] of this result.
   */
  val callReqs get() = reqs.filterIsInstance<UCallReq>()

  /**
   * @return [call requirements][UCallReq] that call [callee], or that overlap with a call to [callee], of this result.
   */
  fun callReqs(callee: UFuncApp, funcs: Funcs) = callReqs.filter { it.equalsOrOverlaps(callee, funcs) }

  /**
   * @return `true` when this result calls [callee], or when it overlaps with a call to [callee], `false` otherwise.
   */
  fun calls(callee: UFuncApp, funcs: Funcs) = callReqs.any { it.equalsOrOverlaps(callee, funcs) }


  /**
   * @return [path generator][Gen] that generates [path] of this result.
   */
  fun gen(path: PPath) = gens.first { it.path == path }

  /**
   * @return `true` when this result generates [path], `false` otherwise.
   */
  fun generates(path: PPath) = gens.any { it.path == path }


  /**
   * @return function application that produced this result.
   */
  val app get() = FuncApp<I, O>(id, input)


  /**
   * @return an [execution reason][ExecReason] when this result is internally inconsistent, or `null` when it is internally consistent.
   */
  val internalInconsistencyReason: ExecReason?
    get() {
      return when(output) {
        is OutTransient<*> -> when {
          output.consistent -> null
          else -> InconsistentTransientOutput(this)
        }
        else -> null
      }
    }

  /**
   * @return `true` if this result is internally inconsistent, `false` if it is internally consistent.
   */
  val isInternallyConsistent: Boolean get() = internalInconsistencyReason == null


  /**
   * @return [output] of this result as a short string, with up to [maxLength] characters.
   */
  fun toShortString(maxLength: Int) = output.toString().toShortString(maxLength)
}


typealias UExecRes = ExecRes<*, *>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I : In, O : Out> UExecRes.cast() = this as ExecRes<I, O>
