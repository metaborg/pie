package mb.pie.lang.runtime

import mb.pie.api.ExecContext
import mb.pie.api.ExecException
import mb.pie.api.stamp.resource.ResourceStampers
import mb.resource.fs.FSPath
import mb.resource.fs.FSResource
import mb.resource.hierarchical.HierarchicalResource
import mb.resource.hierarchical.match.ResourceMatcher
import mb.resource.hierarchical.walk.ResourceWalker
import java.io.IOException
import java.util.stream.Collectors

operator fun FSPath.plus(other: FSPath): FSPath {
  return this.appendRelativePath(other)
}

operator fun FSPath.plus(other: String): FSPath {
  return this.appendSegment(other)
}

fun ExecContext.exists(path: FSPath): Boolean {
  val node = require(path, ResourceStampers.exists<HierarchicalResource>())
  return node.exists()
}

@Throws(ExecException::class)
fun ExecContext.list(path: FSPath, matcher: ResourceMatcher?): ArrayList<FSPath> {
  val node = require(path, ResourceStampers.modified(matcher))
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
fun ExecContext.walk(path: FSPath, walker: ResourceWalker?, matcher: ResourceMatcher?): ArrayList<FSPath> {
  val node = require(path, ResourceStampers.modified(walker, matcher))
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
  val node = require(path, ResourceStampers.hash<HierarchicalResource>())
  try {
    if(!node.exists()) {
      return null
    }
    val bytes = node.readBytes()
    return String(bytes)
  } catch(e: IOException) {
    throw ExecException("Reading '$path' failed", e)
  }
}

@Throws(ExecException::class)
fun ExecContext.copy(from: FSPath, to: FSPath) {
  val fromResource = require(from)
  val toResource = FSResource(to)
  try {
    fromResource.copyTo(toResource)
  } catch(e: IOException) {
    throw ExecException("Copying '$from' to '$to' failed", e)
  }
  provide(to)
}
