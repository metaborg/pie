package mb.pie.api

import java.io.Serializable

/**
 * Serializable none/void type.
 */
class None : Serializable {
  companion object {
    @JvmStatic
    val instance = None()
  }

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return "None()"
  }
}

/**
 * Truncates a string to given [maxLength], and reifies newlines.
 */
fun String.toShortString(maxLength: Int): String {
  val str = this.replace("\r", "\\r").replace("\n", "\\n")
  return if(str.length > maxLength) {
    "${str.substring(0, maxLength - 1)}..."
  } else {
    str
  }
}
