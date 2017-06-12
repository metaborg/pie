package mb.ceres

import java.io.Serializable
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths


interface CPath : Serializable {
  companion object {
    operator fun invoke(path: Path): CPath {
      return CPathImpl(path)
    }

    operator fun invoke(uri: URI): CPath {
      return CPathImpl(uri)
    }
  }

  val uri: URI
  val javaPath: Path
}

data class CPathImpl(override val uri: URI) : CPath {
  constructor(path: Path) : this(path.toUri()) {
    this.pathCache = path
  }

  private @Transient var pathCache: Path? = null

  override val javaPath: Path
    get() {
      if (pathCache == null) {
        pathCache = Paths.get(uri)
      }
      return pathCache!!
    }

  override fun toString(): String {
    return uri.toString()
  }
}