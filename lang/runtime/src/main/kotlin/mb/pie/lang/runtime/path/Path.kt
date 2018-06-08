package mb.pie.lang.runtime.path

import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.list.PathMatcher
import mb.pie.vfs.list.PathWalker
import mb.pie.vfs.path.PPath
import java.io.IOException
import java.io.Serializable
import java.nio.file.Files
import java.util.stream.Collectors


operator fun PPath.plus(other: PPath): PPath {
  return this.resolve(other)
}

operator fun PPath.plus(other: String): PPath {
  return this.resolve(other)
}


class Exists : TaskDef<PPath, Boolean> {
  companion object {
    const val id = "path.Exists"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): Boolean {
    require(input, FileStampers.exists)
    return input.exists()
  }
}

class ListContents : TaskDef<ListContents.Input, ArrayList<PPath>> {
  companion object {
    const val id = "path.ListContents"
  }

  data class Input(val path: PPath, val matcher: PathMatcher?) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<PPath> {
    val (path, matcher) = input
    require(path, FileStampers.modified(matcher))
    if(!path.isDir) {
      throw ExecException("Cannot list contents of '$input', it is not a directory")
    }
    try {
      val stream = if(matcher != null) path.list(matcher) else path.list()
      stream.use {
        return it.collect(Collectors.toCollection { ArrayList<PPath>() })
      }
    } catch(e: IOException) {
      throw ExecException("Cannot list contents of '$input'", e)
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun ListContents.createTask(path: PPath, matcher: PathMatcher?) = Task(this, Input(path, matcher))
}

class WalkContents : TaskDef<WalkContents.Input, ArrayList<PPath>> {
  companion object {
    const val id = "path.WalkContents"
  }

  data class Input(val path: PPath, val walker: PathWalker?) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<PPath> {
    val (path, walker) = input
    require(path, FileStampers.modified(walker))
    if(!path.isDir) {
      throw ExecException("Cannot walk contents of '$input', it is not a directory")
    }
    try {
      val stream = if(walker != null) path.walk(walker) else path.walk()
      stream.use {
        return it.collect(Collectors.toCollection { ArrayList<PPath>() })
      }
    } catch(e: IOException) {
      throw ExecException("Cannot walk contents of '$input'", e)
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun WalkContents.createTask(path: PPath, walker: PathWalker?) = Task(this, Input(path, walker))
}

class Read : TaskDef<PPath, String?> {
  companion object {
    const val id = "path.Read"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): String? {
    require(input, FileStampers.hash)
    try {
      if(!input.exists()) {
        return null
      }
      val bytes = input.readAllBytes()
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

  data class Input(val from: PPath, val to: PPath) : In

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): None {
    val (from, to) = input
    require(from)
    try {
      Files.copy(from.javaPath, to.javaPath)
    } catch(e: IOException) {
      throw ExecException("Copying '${input.from}' to '${input.to}' failed", e)
    }
    generate(to)
    return None.instance
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun Copy.createTask(from: PPath, to: PPath) = Task(this, Input(from, to))
}
