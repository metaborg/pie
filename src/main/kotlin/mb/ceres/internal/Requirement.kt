package mb.ceres.internal

import mb.ceres.*
import java.io.Serializable

interface Req : Serializable {
  fun makeConsistent(build: Build): Boolean
}

internal data class PathReq(val path: CPath, val stamp: PathStamp) : Req {
  override fun makeConsistent(build: Build): Boolean {
    val newStamp = stamp.stamper.stamp(path)
    return stamp == newStamp
  }
}

internal data class BuildReq<out I : In, out O : Out>(val app: BuildApp<I, O>, val stamp: OutputStamp) : Req {
  override fun makeConsistent(build: Build): Boolean {
    val result = build.require(app)
    // CHANGED: paper algorithm did not check if the output changed, which would cause inconsistencies
    val newStamp = stamp.stamper.stamp(result.output)
    return stamp == newStamp
  }
}


data class Gen(val path: CPath, val stamp: PathStamp) : Serializable