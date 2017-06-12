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
}