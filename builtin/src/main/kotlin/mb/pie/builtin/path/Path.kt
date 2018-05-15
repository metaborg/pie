package mb.pie.builtin.path

import com.google.inject.Inject
import mb.pie.runtime.*
import mb.pie.runtime.stamp.FileStampers
import mb.vfs.list.PathMatcher
import mb.vfs.list.PathWalker
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
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

fun ExecContext.exists(input: PPath) = requireOutput(Exists::class.java, Exists.id, input)


class ListContents @Inject constructor(
  val pathSrv: PathSrv
) : TaskDef<ListContents.Input, ArrayList<PPath>> {
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
}

fun ExecContext.listContents(input: ListContents.Input) = requireOutput(ListContents::class.java, ListContents.id, input)


class WalkContents @Inject constructor(
  val pathSrv: PathSrv
) : TaskDef<WalkContents.Input, ArrayList<PPath>> {
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
}

fun ExecContext.walkContents(input: WalkContents.Input) = requireOutput(WalkContents::class.java, WalkContents.id, input)


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

fun ExecContext.read(input: PPath) = requireOutput(Read::class.java, Read.id, input)


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
}

fun ExecContext.copy(input: Copy.Input) = requireOutput(Copy::class.java, Copy.id, input)
