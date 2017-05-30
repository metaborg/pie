package mb.ceres.internal

import mb.ceres.CPath
import mb.ceres.PathStamp
import mb.ceres.PathStamper
import java.nio.file.Files
import java.security.MessageDigest
import java.util.*

class HashPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    val path = cpath.javaPath
    if (Files.notExists(path)) {
      return ByteArrayPathStamp(null, this)
    }

    if (Files.isDirectory(path)) {
      TODO("Implement HashPathStamper for directories")
    }

    val digest = MessageDigest.getInstance("SHA-1")
    val bytes = Files.readAllBytes(path)
    val digestBytes = digest.digest(bytes)
    return ByteArrayPathStamp(digestBytes, this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
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

    return ValuePathStamp(Files.getLastModifiedTime(path).toMillis(), this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

class ExistsPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    return ValuePathStamp(Files.exists(cpath.javaPath), this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}


internal data class ValuePathStamp<out V>(val value: V?, override val stamper: PathStamper) : PathStamp

internal data class ByteArrayPathStamp(val value: ByteArray?, override val stamper: PathStamper) : PathStamp {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as ByteArrayPathStamp

    if (!Arrays.equals(value, other.value)) return false // Override required for array equality
    if (stamper != other.stamper) return false

    return true
  }

  override fun hashCode(): Int {
    var result = value?.let { Arrays.hashCode(it) } ?: 0 // Override required for array hashcode
    result = 31 * result + stamper.hashCode()
    return result
  }
}