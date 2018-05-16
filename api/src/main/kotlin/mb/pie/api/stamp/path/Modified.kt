package mb.pie.api.stamp.path

import mb.pie.api.stamp.FileStamp
import mb.pie.api.stamp.FileStamper
import mb.vfs.list.PathMatcher
import mb.vfs.list.PathWalker
import mb.vfs.path.PPath

interface ModifiedFileStamperTrait : FileStamper {
  val unknown get() = Long.MIN_VALUE

  fun modified(path: PPath, matcher: PathMatcher?): Long {
    if(path.isDir) return modifiedDir(path, matcher)
    if(path.isFile) return path.lastModifiedTimeMs()
    return unknown
  }

  fun modifiedRec(path: PPath, walker: PathWalker?): Long {
    if(path.isDir) return modifiedDirRec(path, walker)
    if(path.isFile) return path.lastModifiedTimeMs()
    return unknown
  }

  fun modifiedDir(dir: PPath, matcher: PathMatcher?): Long {
    if(matcher == null) return dir.lastModifiedTimeMs()
    var lastModified = unknown
    for(subPath in matcher.list(dir)) {
      val modified = subPath.lastModifiedTimeMs()
      lastModified = Math.max(lastModified, modified)
    }
    return lastModified
  }

  fun modifiedDirRec(dir: PPath, walker: PathWalker?): Long {
    var lastModified = unknown
    walker?.walk(dir) ?: dir.walk().use { stream ->
      for(subPath in stream) {
        val modified = subPath.lastModifiedTimeMs()
        lastModified = Math.max(lastModified, modified)
      }
    }
    return lastModified
  }
}

data class ModifiedFileStamper(private val matcher: PathMatcher? = null) : ModifiedFileStamperTrait {
  override fun stamp(path: PPath): FileStamp {
    if(!path.exists()) {
      return ValueFileStamp(null, this)
    }
    val modified = modified(path, matcher)
    return ValueFileStamp(modified, this)
  }

  override fun toString(): String {
    return "Modified($matcher)"
  }
}

data class RecModifiedFileStamper(private val walker: PathWalker? = null) : ModifiedFileStamperTrait {
  override fun stamp(path: PPath): FileStamp {
    if(!path.exists()) {
      return ValueFileStamp(null, this)
    }
    val modified = modifiedRec(path, walker)
    return ValueFileStamp(modified, this)
  }

  override fun toString(): String {
    return "RecModified($walker)"
  }
}
