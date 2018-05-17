package mb.pie.api.stamp

import mb.pie.api.stamp.path.*
import mb.pie.vfs.list.PathMatcher
import mb.pie.vfs.list.PathWalker
import mb.pie.vfs.path.PPath
import java.io.Serializable

/**
 * Stamper for customizable change detection on file contents. Stampers must be [Serializable].
 */
interface FileStamper : Serializable {
  fun stamp(path: PPath): FileStamp
}

/**
 * Stamp produced by a [FileStamper]. Stamps must be [Serializable].
 */
interface FileStamp : Serializable {
  val stamper: FileStamper
}

/**
 * Common file stampers.
 */
object FileStampers {
  val hash = HashFileStamper()
  fun hash(matcher: PathMatcher?) = HashFileStamper(matcher)
  fun hash(walker: PathWalker?) = RecHashFileStamper(walker)

  val modified = ModifiedFileStamper()
  fun modified(matcher: PathMatcher?) = ModifiedFileStamper(matcher)
  fun modified(walker: PathWalker?) = RecModifiedFileStamper(walker)

  val exists = ExistsFileStamper()
}
