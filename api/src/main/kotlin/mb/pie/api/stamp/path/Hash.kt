package mb.pie.api.stamp.path

import mb.pie.api.stamp.FileStamp
import mb.pie.api.stamp.FileStamper
import mb.pie.vfs.list.PathMatcher
import mb.pie.vfs.list.PathWalker
import mb.pie.vfs.path.PPath
import java.security.MessageDigest

interface HashFileStamperTrait : FileStamper {
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

data class HashFileStamper(private val matcher: PathMatcher? = null) : HashFileStamperTrait {
  override fun stamp(path: PPath): FileStamp {
    if(!path.exists()) {
      return ByteArrayFileStamp(null, this)
    }
    val digest = createDigester()
    digest.update(path, matcher)
    val bytes = digest.digest()
    return ByteArrayFileStamp(bytes, this)
  }

  override fun toString(): String {
    return "Hash($matcher)"
  }
}

data class RecHashFileStamper(private val walker: PathWalker? = null) : HashFileStamperTrait {
  override fun stamp(path: PPath): FileStamp {
    if(!path.exists()) {
      return ByteArrayFileStamp(null, this)
    }
    val digest = createDigester()
    digest.updateRec(path, walker)
    val bytes = digest.digest()
    return ByteArrayFileStamp(bytes, this)
  }

  override fun toString(): String {
    return "RecHash($walker)"
  }
}