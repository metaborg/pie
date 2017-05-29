package mb.ceres

import mb.ceres.internal.ExistsPathStamper
import mb.ceres.internal.HashPathStamper
import mb.ceres.internal.ModifiedPathStamper
import java.io.Serializable

interface PathStamper {
  fun stamp(cpath: CPath): PathStamp
}

interface PathStamp : Serializable {
  val stamper: PathStamper
}

object PathStampers {
  val hash = HashPathStamper()
  val modified = ModifiedPathStamper()
  val exists = ExistsPathStamper()
}