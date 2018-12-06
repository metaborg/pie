package mb.pie.api.fs.stamp

import mb.fs.api.node.*
import mb.fs.api.node.match.FSNodeMatcher
import mb.fs.api.node.walk.FSNodeWalker
import mb.pie.api.fs.FileSystemResource

interface ModifiedResourceStamperTrait : FileSystemStamper {
  val unknown get() = Long.MIN_VALUE

  fun modified(node: FSNode, matcher: FSNodeMatcher?): Long {
    if(node.isDirectory) return modifiedDir(node, matcher)
    if(node.isFile) return node.lastModifiedTime.toEpochMilli()
    return unknown
  }

  fun modifiedRec(node: FSNode, walker: FSNodeWalker?, matcher: FSNodeMatcher?): Long {
    if(node.isDirectory) return modifiedDirRec(node, walker, matcher)
    if(node.isFile) return node.lastModifiedTime.toEpochMilli()
    return unknown
  }

  fun modifiedDir(dir: FSNode, matcher: FSNodeMatcher?): Long {
    if(matcher == null) return dir.lastModifiedTime.toEpochMilli()
    var lastModified = unknown
    dir.list(matcher).use { stream ->
      for(subPath in stream) {
        val modified = subPath.lastModifiedTime.toEpochMilli()
        lastModified = Math.max(lastModified, modified)
      }
    }
    return lastModified
  }

  fun modifiedDirRec(dir: FSNode, walker: FSNodeWalker?, matcher: FSNodeMatcher?): Long {
    var lastModified = unknown
    if(walker == null || matcher == null) {
      dir.walk()
    } else {
      dir.walk(walker, matcher)
    }.use { stream ->
      for(subPath in stream) {
        val modified = subPath.lastModifiedTime.toEpochMilli()
        lastModified = Math.max(lastModified, modified)
      }
    }
    return lastModified
  }
}

data class ModifiedResourceStamper @JvmOverloads constructor(
  private val matcher: FSNodeMatcher? = null
) : ModifiedResourceStamperTrait {
  override fun stamp(resource: FileSystemResource): FileSystemStamp {
    val node = resource.node
    if(!node.exists()) {
      return ValueResourceStamp(null, this)
    }
    val modified = modified(node, matcher)
    return ValueResourceStamp(modified, this)
  }

  override fun toString(): String {
    return "Modified($matcher)"
  }
}

data class RecModifiedResourceStamper @JvmOverloads constructor(
  private val walker: FSNodeWalker? = null,
  private val matcher: FSNodeMatcher? = null
) : ModifiedResourceStamperTrait {
  override fun stamp(resource: FileSystemResource): FileSystemStamp {
    val node = resource.node
    if(!node.exists()) {
      return ValueResourceStamp(null, this)
    }
    val modified = modifiedRec(node, walker, matcher)
    return ValueResourceStamp(modified, this)
  }

  override fun toString(): String {
    return "RecModified($walker)"
  }
}
