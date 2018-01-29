package mb.pie.runtime.core.impl.stamp.path

import mb.pie.runtime.core.stamp.PathStamp
import mb.pie.runtime.core.stamp.PathStamper
import mb.vfs.list.PathMatcher
import mb.vfs.list.PathWalker
import mb.vfs.path.PPath
import java.security.MessageDigest

interface HashPathStamperTrait : PathStamper {
  fun createDigester() = MessageDigest.getInstance("SHA-1")!!

  fun MessageDigest.update(path: PPath, matcher: PathMatcher?) {
    if(path.isDir) updateDir(path, matcher)
    if(path.isFile) updateFile(path)
  }

  fun MessageDigest.updateRec(path: PPath, walker: PathWalker?) {
    if(path.isDir) updateDirRec(path, walker)
    if(path.isFile) updateFile(path)
  }

  fun MessageDigest.updateDir(dir: PPath, matcher: PathMatcher?) {
    matcher?.list(dir) ?: dir.list().use { stream ->
      for(subPath in stream) {
        if(subPath.isFile) updateFile(subPath)
      }
    }
  }

  fun MessageDigest.updateDirRec(dir: PPath, walker: PathWalker?) {
    walker?.walk(dir) ?: dir.walk().use { stream ->
      for(subPath in stream) {
        if(subPath.isFile) updateFile(subPath)
      }
    }
  }

  fun MessageDigest.updateFile(file: PPath) {
    update(file.readAllBytes())
  }
}

data class HashPathStamper(private val matcher: PathMatcher? = null) : HashPathStamperTrait {
  override fun stamp(path: PPath): PathStamp {
    if(!path.exists()) {
      return ByteArrayPathStamp(null, this)
    }
    val digest = createDigester()
    digest.update(path, matcher)
    val bytes = digest.digest()
    return ByteArrayPathStamp(bytes, this)
  }

  override fun toString(): String {
    return "Hash($matcher)"
  }
}

data class RecHashPathStamper(private val walker: PathWalker? = null) : HashPathStamperTrait {
  override fun stamp(path: PPath): PathStamp {
    if(!path.exists()) {
      return ByteArrayPathStamp(null, this)
    }
    val digest = createDigester()
    digest.updateRec(path, walker)
    val bytes = digest.digest()
    return ByteArrayPathStamp(bytes, this)
  }

  override fun toString(): String {
    return "RecHash($walker)"
  }
}