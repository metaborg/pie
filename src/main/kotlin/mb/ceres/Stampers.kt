package mb.ceres

import java.nio.file.Files
import java.security.MessageDigest

class PathHashStamper : Stamper {
  override fun stamp(path: CPath): Stamp {
    if (Files.notExists(path.javaPath)) {
      return ValueStamp(null, this)
    }

    if (Files.isDirectory(path.javaPath)) {
      TODO("Implement PathHashStamper for directories")
    }

    val digest = MessageDigest.getInstance("SHA-1")
    val bytes = Files.readAllBytes(path.javaPath)
    val digestBytes = digest.digest(bytes)
    return ValueStamp(digestBytes, this)
  }
}

class PathExistsStamper : Stamper {
  override fun stamp(path: CPath): Stamp {
    return ValueStamp(Files.exists(path.javaPath), this)
  }
}
