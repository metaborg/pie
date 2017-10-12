package mb.pie.runtime.core.impl.stamp

import mb.pie.runtime.core.PathStamp
import mb.pie.runtime.core.PathStamper
import mb.vfs.list.PathMatcher
import mb.vfs.list.PathWalker
import mb.vfs.path.PPath

interface ModifiedPathStamperTrait : PathStamper {
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
    for(subPath in walker?.walk(dir) ?: dir.walk()) {
      val modified = subPath.lastModifiedTimeMs()
      lastModified = Math.max(lastModified, modified)
    }
    return lastModified
  }
}

data class ModifiedPathStamper(private val matcher: PathMatcher? = null) : ModifiedPathStamperTrait {
  override fun stamp(path: PPath): PathStamp {
    if(!path.exists()) {
      return ValuePathStamp(null, this)
    }
    val modified = modified(path, matcher)
    return ValuePathStamp(modified, this)
  }
}

data class RecModifiedPathStamper(private val walker: PathWalker? = null) : ModifiedPathStamperTrait {
  override fun stamp(path: PPath): PathStamp {
    if(!path.exists()) {
      return ValuePathStamp(null, this)
    }
    val modified = modifiedRec(path, walker)
    return ValuePathStamp(modified, this)
  }
}
