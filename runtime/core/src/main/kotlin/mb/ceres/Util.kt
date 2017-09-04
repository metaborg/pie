package mb.ceres

import java.io.Serializable

class None : Serializable {
  companion object {
    val instance = None()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return "None()"
  }
}

fun String.toShortString(maxLength: Int): String {
  val str = this.replace("\r", "\\r").replace("\n", "\\n")
  return if (str.length > maxLength) {
    "${str.substring(0, maxLength - 1)}..."
  } else {
    str
  }
}