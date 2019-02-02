package mb.pie.api.fs.stamp

import mb.fs.api.node.match.FSNodeMatcher
import mb.fs.api.node.walk.FSNodeWalker
import mb.pie.api.fs.FileSystemResource
import mb.pie.api.stamp.ResourceStamper
import java.time.Duration
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


  /**
   * File is considered modified after [duration] time has passed.
   * The case duration = Zero can be usefull for testing
   */

  fun time_since_used( duration: Duration ) = TimeSinceUsedResourceStamper(duration)
  val always_dirty = TimeSinceUsedResourceStamper( Duration.ZERO )

}

/**
 * Resource stamper that supports [file system resources][FileSystemResource].
 */
typealias FileSystemStamper = ResourceStamper<FileSystemResource>
