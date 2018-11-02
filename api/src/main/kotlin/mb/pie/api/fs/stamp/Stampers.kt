package mb.pie.api.fs.stamp

import mb.fs.api.node.FSNodeMatcher
import mb.fs.api.node.FSNodeWalker
import mb.pie.api.fs.FileSystemResource
import mb.pie.api.stamp.ResourceStamper

/**
 * Common file system stampers.
 */
object FileSystemStampers {
  val hash = HashResourceStamper()
  fun hash(matcher: FSNodeMatcher?) = HashResourceStamper(matcher)
  fun hash(walker: FSNodeWalker?, matcher: FSNodeMatcher?) = RecHashResourceStamper(walker, matcher)

  val modified = ModifiedResourceStamper()
  fun modified(matcher: FSNodeMatcher?) = ModifiedResourceStamper(matcher)
  fun modified(walker: FSNodeWalker?, matcher: FSNodeMatcher?) = RecModifiedResourceStamper(walker, matcher)

  val exists = ExistsResourceStamper()
}

/**
 * Resource stamper that supports [file system resources][FileSystemResource].
 */
typealias FileSystemStamper = ResourceStamper<FileSystemResource>
