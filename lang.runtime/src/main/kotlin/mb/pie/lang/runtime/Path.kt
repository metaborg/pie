package mb.pie.lang.runtime

import mb.fs.api.node.match.FSNodeMatcher
import mb.fs.api.node.walk.FSNodeWalker
import mb.fs.java.JavaFSPath
import mb.pie.api.ExecContext
import mb.pie.api.ExecException
import mb.pie.api.fs.stamp.FileSystemStampers
import java.io.IOException
import java.util.stream.Collectors

operator fun JavaFSPath.plus(other: JavaFSPath): JavaFSPath {
  return this.appendRelativePath(other)
}

operator fun JavaFSPath.plus(other: String): JavaFSPath {
  return this.appendSegment(other)
}

fun ExecContext.exists(path: JavaFSPath): Boolean {
  val node = require(path, FileSystemStampers.getExists())
  return node.exists()
}

@Throws(ExecException::class)
fun ExecContext.list(path: JavaFSPath, matcher: FSNodeMatcher?): ArrayList<JavaFSPath> {
  val node = require(path, FileSystemStampers.modified(matcher))
  if(!node.isDirectory) {
    throw ExecException("Cannot list '$path', it is not a directory")
  }
  try {
    val nodes = if(matcher != null) node.list(matcher) else node.list()
    nodes.use { stream ->
      return stream.map { it.path }.collect(Collectors.toCollection { ArrayList<JavaFSPath>() })
    }
  } catch(e: IOException) {
    throw ExecException("Cannot list '$path'", e)
  }
}

@Throws(ExecException::class)
fun ExecContext.walk(path: JavaFSPath, walker: FSNodeWalker?, matcher: FSNodeMatcher?): ArrayList<JavaFSPath> {
  val node = require(path, FileSystemStampers.modified(walker, matcher))
  if(!node.isDirectory) {
    throw ExecException("Cannot walk '$path', it is not a directory")
  }
  try {
    val nodes = if(walker != null && matcher != null) node.walk(walker, matcher) else node.walk()
    nodes.use { stream ->
      return stream.map { it.path }.collect(Collectors.toCollection { ArrayList<JavaFSPath>() })
    }
  } catch(e: IOException) {
    throw ExecException("Cannot walk '$path'", e)
  }
}

@Throws(ExecException::class)
fun ExecContext.readToString(path: JavaFSPath): String? {
  val node = require(path, FileSystemStampers.getHash())
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
fun ExecContext.copy(from: JavaFSPath, to: JavaFSPath) {
  val fromNode = require(from)
  val toNode = toNode(to)
  try {
    fromNode.copyTo(toNode)
  } catch(e: IOException) {
    throw ExecException("Copying '$from' to '$to' failed", e)
  }
  provide(to)
}
