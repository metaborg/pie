package mb.pie.api.fs.stamp

import mb.fs.api.node.*
import mb.fs.api.node.match.FSNodeMatcher
import mb.fs.api.node.walk.FSNodeWalker
import mb.pie.api.fs.FileSystemResource
import java.security.MessageDigest

interface HashResourceStamperTrait : FileSystemStamper {
  fun createDigester() = MessageDigest.getInstance("SHA-1")!!

  fun MessageDigest.update(node: FSNode, matcher: FSNodeMatcher?) {
    if(node.isDirectory) updateDir(node, matcher)
    if(node.isFile) updateFile(node)
  }

  fun MessageDigest.updateRec(node: FSNode, walker: FSNodeWalker?, matcher: FSNodeMatcher?) {
    if(node.isDirectory) updateDirRec(node, walker, matcher)
    if(node.isFile) updateFile(node)
  }

  fun MessageDigest.updateDir(dir: FSNode, matcher: FSNodeMatcher?) {
    if(matcher == null) {
      dir.list()
    } else {
      dir.list(matcher)
    }.use { stream ->
      for(node in stream) {
        if(node.isFile) updateFile(node)
      }
    }
  }

  fun MessageDigest.updateDirRec(dir: FSNode, walker: FSNodeWalker?, matcher: FSNodeMatcher?) {
    if(walker == null || matcher == null) {
      dir.walk()
    } else {
      dir.walk(walker, matcher)
    }.use { stream ->
      for(node in stream) {
        if(node.isFile) updateFile(node)
      }
    }
  }

  fun MessageDigest.updateFile(file: FSNode) {
    update(file.readAllBytes())
  }
}

data class HashResourceStamper @JvmOverloads constructor(
  private val matcher: FSNodeMatcher? = null
) : HashResourceStamperTrait {
  override fun stamp(resource: FileSystemResource): FileSystemStamp {
    val node = resource.node
    if(!node.exists()) {
      return ByteArrayResourceStamp(null, this)
    }
    val digest = createDigester()
    digest.update(node, matcher)
    val bytes = digest.digest()
    return ByteArrayResourceStamp(bytes, this)
  }

  override fun toString(): String {
    return "Hash($matcher)"
  }
}

data class RecHashResourceStamper @JvmOverloads constructor(
  private val walker: FSNodeWalker? = null,
  private val matcher: FSNodeMatcher? = null
) : HashResourceStamperTrait {
  override fun stamp(resource: FileSystemResource): FileSystemStamp {
    val node = resource.node
    if(!node.exists()) {
      return ByteArrayResourceStamp(null, this)
    }
    val digest = createDigester()
    digest.updateRec(node, walker, matcher)
    val bytes = digest.digest()
    return ByteArrayResourceStamp(bytes, this)
  }

  override fun toString(): String {
    return "RecHash($walker)"
  }
}