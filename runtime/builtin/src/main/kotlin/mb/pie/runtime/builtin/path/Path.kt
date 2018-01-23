package mb.pie.runtime.builtin.path

import com.google.inject.Inject
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
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


class Exists : Func<PPath, Boolean> {
  companion object {
    val id = "exists"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): Boolean {
    require(input, PathStampers.exists)
    return input.exists()
  }
}

fun ExecContext.exists(input: PPath) = requireOutput(Exists::class, Exists.Companion.id, input)


class ListContents @Inject constructor(val pathSrv: PathSrv) : Func<ListContents.Input, ArrayList<PPath>> {
  companion object {
    val id = "listContents"
  }

  data class Input(val path: PPath, val matcher: PathMatcher?) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<PPath> {
    val (path, matcher) = input
    require(path, PathStampers.modified(matcher))
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

fun ExecContext.listContents(input: ListContents.Input) = requireOutput(ListContents::class, ListContents.Companion.id, input)


class WalkContents @Inject constructor(val pathSrv: PathSrv) : Func<WalkContents.Input, ArrayList<PPath>> {
  companion object {
    val id = "walkContents"
  }

  data class Input(val path: PPath, val walker: PathWalker?) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<PPath> {
    val (path, walker) = input
    require(path, PathStampers.modified(walker))
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

fun ExecContext.walkContents(input: WalkContents.Input) = requireOutput(WalkContents::class, WalkContents.Companion.id, input)


class Read : Func<PPath, String?> {
  companion object {
    val id = "read"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): String? {
    require(input, PathStampers.hash)
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

fun ExecContext.read(input: PPath) = requireOutput(Read::class, Read.Companion.id, input)


class Copy : OutEffectFunc<Copy.Input> {
  companion object {
    val id = "copy"
  }

  data class Input(val from: PPath, val to: PPath) : In

  override val id = Companion.id
  override fun ExecContext.effect(input: Input) {
    val (from, to) = input
    require(from)
    try {
      Files.copy(from.javaPath, to.javaPath)
    } catch(e: IOException) {
      throw ExecException("Copying '${input.from}' to '${input.to}' failed", e)
    }
    generate(to)
  }
}

fun ExecContext.copy(input: Copy.Input) = requireOutput(Copy::class, Copy.Companion.id, input)
