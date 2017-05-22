package mb.ceres

class None : Out {
  companion object {
    val instance = None()
  }

  override fun equals(other: Any?): Boolean {
    return other is None
  }

  override fun hashCode(): Int {
    return 0
  }
}