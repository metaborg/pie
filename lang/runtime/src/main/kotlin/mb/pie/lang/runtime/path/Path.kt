package mb.pie.lang.runtime.path

import mb.fs.api.node.FSNodeMatcher
import mb.fs.api.node.FSNodeWalker
import mb.fs.api.path.FSPath
import mb.fs.api.path.RelativeFSPath
import mb.pie.api.*
import mb.pie.api.fs.stamp.FileSystemStampers
import java.io.IOException
import java.io.Serializable
import java.util.stream.Collectors

operator fun RelativeFSPath.plus(other: RelativeFSPath): RelativeFSPath {
  return this.appendRelativePath(other)
}

operator fun RelativeFSPath.plus(other: String): RelativeFSPath {
  return this.appendSegment(other)
}

operator fun FSPath.plus(other: FSPath): FSPath {
  // HACK: appending two absolute paths
  return this.appendSegments(other.segments)
}

operator fun FSPath.plus(other: RelativeFSPath): FSPath {
  return this.appendRelativePath(other)
}

operator fun FSPath.plus(other: String): FSPath {
  return this.appendSegment(other)
}


class Exists : TaskDef<FSPath, Boolean> {
  companion object {
    const val id = "path.Exists"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: FSPath): Boolean {
    val node = require(input, FileSystemStampers.exists)
    return node.exists()
  }
}

class ListContents : TaskDef<ListContents.Input, ArrayList<FSPath>> {
  companion object {
    const val id = "path.ListContents"
  }

  data class Input(val path: FSPath, val matcher: FSNodeMatcher?) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<FSPath> {
    val (path, matcher) = input
    val node = require(path, FileSystemStampers.modified(matcher))
    if(!node.isDirectory) {
      throw ExecException("Cannot list contents of '$input', it is not a directory")
    }
    try {
      val stream = if(matcher != null) node.list(matcher) else node.list()
      stream.use {
        return it.map { it.path }.collect(Collectors.toCollection { ArrayList<FSPath>() })
      }
    } catch(e: IOException) {
      throw ExecException("Cannot list contents of '$input'", e)
    }
  }

  fun createTask(path: FSPath, matcher: FSNodeMatcher?) = Task(this, Input(path, matcher))
}

class WalkContents : TaskDef<WalkContents.Input, ArrayList<FSPath>> {
  companion object {
    const val id = "path.WalkContents"
  }

  data class Input(val path: FSPath, val walker: FSNodeWalker?, val matcher: FSNodeMatcher?) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<FSPath> {
    val (path, walker, matcher) = input
    val node = require(path, FileSystemStampers.modified(walker, matcher))
    if(!node.isDirectory) {
      throw ExecException("Cannot walk contents of '$input', it is not a directory")
    }
    try {
      val stream = if(walker != null && matcher != null) node.walk(walker, matcher) else node.walk()
      stream.use {
        return it.map { it.path }.collect(Collectors.toCollection { ArrayList<FSPath>() })
      }
    } catch(e: IOException) {
      throw ExecException("Cannot walk contents of '$input'", e)
    }
  }

  fun createTask(path: FSPath, walker: FSNodeWalker?, matcher: FSNodeMatcher?) = Task(this, Input(path, walker, matcher))
}

class Read : TaskDef<FSPath, String?> {
  companion object {
    const val id = "path.Read"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: FSPath): String? {
    val node = require(input, FileSystemStampers.hash)
    try {
      if(!node.exists()) {
        return null
      }
      val bytes = node.readAllBytes()
      return String(bytes)
    } catch(e: IOException) {
      throw ExecException("Reading '$input' failed", e)
    }
  }
}

class Copy : TaskDef<Copy.Input, None> {
  companion object {
    const val id = "path.Copy"
  }

  data class Input(val from: FSPath, val to: FSPath) : In

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): None {
    val (from, to) = input
    val fromNode = require(from)
    val toNode = toNode(to)
    try {
      fromNode.copyTo(toNode)
    } catch(e: IOException) {
      throw ExecException("Copying '$from' to '$to' failed", e)
    }
    provide(to)
    return None.instance
  }

  fun createTask(from: FSPath, to: FSPath) = Task(this, Input(from, to))
}
