package mb.ceres

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path

data class CPath(var pathStr: String, var scheme: String) : Serializable {
  constructor(path: Path) : this(path.toString(), path.fileSystem.provider().scheme) {
    this.pathCache = path
  }

  private @Transient var pathCache: Path? = null

  val javaPath: Path
    get() {
      if (pathCache != null) {
        return pathCache as Path
      } else {
        val fs = FileSystems.getFileSystem(URI(scheme, "", ""))
        val path = fs.getPath(pathStr)
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
