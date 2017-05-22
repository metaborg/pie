package mb.ceres

import java.io.Serializable
import java.nio.file.Files
import java.security.MessageDigest


interface PathStamper {
  fun stamp(cpath: CPath): PathStamp
}

interface PathStamp : Serializable {
  val stamper: PathStamper
}

data class ValuePathStamp<out V>(val value: V?, override val stamper: PathStamper) : PathStamp


class HashPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    val path = cpath.javaPath
    if (Files.notExists(path)) {
      return ValuePathStamp(null, this)
    }

    if (Files.isDirectory(path)) {
      TODO("Implement HashPathStamper for directories")
    }

    val digest = MessageDigest.getInstance("SHA-1")
    val bytes = Files.readAllBytes(path)
    val digestBytes = digest.digest(bytes)
    return ValuePathStamp(digestBytes, this)
  }
}

class ModifiedPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    val path = cpath.javaPath
    if (Files.notExists(path)) {
      return ValuePathStamp(null, this)
    }

    if (Files.isDirectory(path)) {
      TODO("Implement ModifiedPathStamper for directories")
    }

    return ValuePathStamp(Files.getLastModifiedTime(path), this)
  }
}

class ExistsPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    return ValuePathStamp(Files.exists(cpath.javaPath), this)
  }
}
