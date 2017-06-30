package mb.ceres

import mb.ceres.impl.DirectoriesModifiedPathStamper
import mb.ceres.impl.ExistsPathStamper
import mb.ceres.impl.HashPathStamper
import mb.ceres.impl.ModifiedPathStamper
import mb.ceres.impl.NonRecursiveModifiedPathStamper
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