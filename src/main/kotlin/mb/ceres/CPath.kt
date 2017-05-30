package mb.ceres

import java.io.Serializable
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

data class CPath(private var uri: URI) : Serializable {
  constructor(path: Path) : this(path.toUri()) {
    this.pathCache = path
  }

  private @Transient var pathCache: Path? = null

  val javaPath: Path
    get() {
      if (pathCache != null) {
        return pathCache!!
      } else {
        val path = Paths.get(uri)
        pathCache = path
        return path
      }
    }

  fun resolve(other: String): CPath {
    val path = this.javaPath
    val newPath = path.resolve(other)
    return CPath(newPath)
  }
}
