package mb.ceres

import mb.ceres.impl.stamp.*
import mb.vfs.list.PathMatcher
import mb.vfs.list.PathWalker
import mb.vfs.path.PPath
import java.io.Serializable

interface PathStamper : Serializable {
  fun stamp(path: PPath): PathStamp
}

interface PathStamp : Serializable {
  val stamper: PathStamper
}

object PathStampers {
  val hash = HashPathStamper()
  fun hash(matcher: PathMatcher?) = HashPathStamper(matcher)
  fun hash(walker: PathWalker?) = RecHashPathStamper(walker)

  val modified = ModifiedPathStamper()
  fun modified(matcher: PathMatcher?) = ModifiedPathStamper(matcher)
  fun modified(walker: PathWalker?) = RecModifiedPathStamper(walker)

  val exists = ExistsPathStamper()
}