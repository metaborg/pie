package mb.ceres.internal

import mb.ceres.BuildApp
import mb.ceres.In
import mb.ceres.Out
import mb.ceres.UBuildApp
import java.io.Serializable

data class BuildRes<out I : In, out O : Out>(val builderId: String, val desc: String, val input: I, val output: O, val reqs: List<Req>, val gens: List<Gen>) : Serializable {
  val toApp get() = BuildApp<I, O>(builderId, input)
  fun requires(other: UBuildApp): Boolean {
    for ((req, _) in reqs.filterIsInstance<UBuildReq>()) {
      if (other == req) {
        return true
      }
    }
    return false
  }
}
typealias UBuildRes = BuildRes<*, *>