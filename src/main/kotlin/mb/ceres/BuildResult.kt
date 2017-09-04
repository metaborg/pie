package mb.ceres

import mb.ceres.impl.Gen
import mb.ceres.impl.Req
import mb.ceres.impl.UBuildReq
import java.io.Serializable

data class BuildRes<out I : In, out O : Out>(val builderId: String, val desc: String, val input: I, val output: O, val reqs: List<Req>, val gens: List<Gen>) : Serializable {
  val toApp get() = BuildApp<I, O>(builderId, input)

  val inconsistencyReason: BuildReason? get() {
    if (output is OutTransient<*>) {
      return if (output.consistent) null else InconsistentTransientOutput(this)
    }
    return null
  }
  val isConsistent: Boolean get() = inconsistencyReason == null

  fun requires(other: UBuildApp): Boolean {
    for ((req, _) in reqs.filterIsInstance<UBuildReq>()) {
      if (other == req) {
        return true
      }
    }
    return false
  }

  fun toShortString(maxLength: Int) = output.toString().toShortString(maxLength)
}

typealias UBuildRes = BuildRes<*, *>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I : In, O : Out> UBuildRes.cast() = this as BuildRes<I, O>
