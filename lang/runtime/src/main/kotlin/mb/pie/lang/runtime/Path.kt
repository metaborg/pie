package mb.pie.lang.runtime

import mb.fs.api.node.FSNodeMatcher
import mb.fs.api.node.FSNodeWalker
import mb.fs.api.path.FSPath
import mb.fs.java.JavaFSPath
import mb.pie.api.ExecContext
import mb.pie.api.ExecException
import mb.pie.api.fs.stamp.FileSystemStampers
import java.io.IOException
import java.util.stream.Collectors

operator fun FSPath.plus(other: FSPath): FSPath {
  return this.appendRelativePath(other)
}

operator fun FSPath.plus(other: String): FSPath {
  return this.appendSegment(other)
}

fun ExecContext.exists(path: JavaFSPath): Boolean {
  val node = require(path, FileSystemStampers.exists)
  return node.exists()
}

@Throws(ExecException::class)
fun ExecContext.list(path: FSPath, matcher: FSNodeMatcher?): ArrayList<FSPath> {
  val node = require(path, FileSystemStampers.modified(matcher))
  if(!node.isDirectory) {
    throw ExecException("Cannot list '$path', it is not a directory")
  }
  try {
    val nodes = if(matcher != null) node.list(matcher) else node.list()
    nodes.use { stream ->
      return stream.map { it.path }.collect(Collectors.toCollection { ArrayList<FSPath>() })
    }
  } catch(e: IOException) {
    throw ExecException("Cannot list '$path'", e)
  }
}

@Throws(ExecException::class)
fun ExecContext.walk(path: FSPath, walker: FSNodeWalker?, matcher: FSNodeMatcher?): ArrayList<FSPath> {
  val node = require(path, FileSystemStampers.modified(walker, matcher))
  if(!node.isDirectory) {
    throw ExecException("Cannot walk '$path', it is not a directory")
  }
  try {
    val nodes = if(walker != null && matcher != null) node.walk(walker, matcher) else node.walk()
    nodes.use { stream ->
      return stream.map { it.path }.collect(Collectors.toCollection { ArrayList<FSPath>() })
    }
  } catch(e: IOException) {
    throw ExecException("Cannot walk '$path'", e)
  }
}

@Throws(ExecException::class)
fun ExecContext.readToString(path: FSPath): String? {
  val node = require(path, FileSystemStampers.hash)
  try {
    if(!node.exists()) {
      return null
    }
    val bytes = node.readAllBytes()
    return String(bytes)
  } catch(e: IOException) {
    throw ExecException("Reading '$path' failed", e)
  }
}

@Throws(ExecException::class)
fun ExecContext.copy(from: FSPath, to: FSPath) {
  val fromNode = require(from)
  val toNode = toNode(to)
  try {
    fromNode.copyTo(toNode)
  } catch(e: IOException) {
    throw ExecException("Copying '$from' to '$to' failed", e)
  }
  provide(to)
}
