package mb.ceres.internal

import mb.ceres.CPath
import mb.ceres.PathStamp
import mb.ceres.PathStamper
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Arrays

class HashPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    val path = cpath.javaPath
    if (Files.notExists(path)) {
      return ByteArrayPathStamp(null, this)
    }
    val digest = MessageDigest.getInstance("SHA-1")
    digest.digestPath(path)
    val bytes = digest.digest()
    return ByteArrayPathStamp(bytes, this)
  }

  private fun MessageDigest.digestFile(path: Path) {
    val bytes = Files.readAllBytes(path)
    update(bytes)
  }

  private fun MessageDigest.digestDir(path: Path) {
    for (subPath in Files.list(path)) {
      digestPath(subPath)
    }
  }

  private fun MessageDigest.digestPath(path: Path) {
    if (Files.notExists(path)) {
      return; // Don't digest non-existant files
    }
    if (Files.isDirectory(path)) {
      return digestDir(path)
    }
    digestFile(path)
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
    val lastModifiedTime = pathLastModified(path)
    return ValuePathStamp(lastModifiedTime, this)
  }

  private fun fileLastModified(path: Path): Long {
    return Files.getLastModifiedTime(path).toMillis();
  }

  private fun dirLastModified(path: Path): Long {
    var latestModifiedTime = Files.getLastModifiedTime(path).toMillis();
    for (subPath in Files.list(path)) {
      val modifiedTime = pathLastModified(subPath)
      latestModifiedTime = Math.max(latestModifiedTime, modifiedTime)
    }
    return latestModifiedTime
  }

  private fun pathLastModified(path: Path): Long {
    if (Files.notExists(path)) {
      // Non existant files do not have a modification time.
      return Long.MIN_VALUE
    }
    if (Files.isDirectory(path)) {
      return dirLastModified(path)
    }
    return fileLastModified(path)
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

class DirectoriesModifiedPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    val path = cpath.javaPath
    if (Files.notExists(path)) {
      return ValuePathStamp(null, this)
    }
    val lastModifiedTime = pathLastModified(path)
    return ValuePathStamp(lastModifiedTime, this)
  }

  private fun dirLastModified(path: Path): Long {
    var latestModifiedTime = Files.getLastModifiedTime(path).toMillis();
    for (subPath in Files.list(path)) {
      val modifiedTime = pathLastModified(subPath)
      latestModifiedTime = Math.max(latestModifiedTime, modifiedTime)
    }
    return latestModifiedTime
  }

  private fun pathLastModified(path: Path): Long {
    if (Files.isDirectory(path)) {
      return dirLastModified(path)
    }
    // Ignore files and non-existant paths
    return Long.MIN_VALUE;
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

class NonRecursiveModifiedPathStamper : PathStamper {
  override fun stamp(cpath: CPath): PathStamp {
    val path = cpath.javaPath
    if (Files.notExists(path)) {
      return ValuePathStamp(null, this)
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


data class ValuePathStamp<out V>(val value: V?, override val stamper: PathStamper) : PathStamp

data class ByteArrayPathStamp(val value: ByteArray?, override val stamper: PathStamper) : PathStamp {
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