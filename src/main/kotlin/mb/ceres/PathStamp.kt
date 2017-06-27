package mb.ceres

import mb.ceres.internal.DirectoriesModifiedPathStamper
import mb.ceres.internal.ExistsPathStamper
import mb.ceres.internal.HashPathStamper
import mb.ceres.internal.ModifiedPathStamper
import mb.ceres.internal.NonRecursiveModifiedPathStamper
import java.io.Serializable

interface PathStamper : Serializable {
  fun stamp(cpath: CPath): PathStamp
}

interface PathStamp : Serializable {
  val stamper: PathStamper
}

object PathStampers {
  val hash = HashPathStamper()
  val modified = ModifiedPathStamper()
  val directoriesModified = DirectoriesModifiedPathStamper()
  val nonRecursiveModified = NonRecursiveModifiedPathStamper()
  val exists = ExistsPathStamper()
}