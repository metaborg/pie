package mb.pie.runtime.core.impl

import mb.pie.runtime.core.*


class EqualsOutputStamper : OutputStamper {
  override fun <O : Out> stamp(output: O): OutputStamp {
    return ValueOutputStamp(output, this)
  }

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

class InconsequentialOutputStamper : OutputStamper {
  companion object {
    val instance = InconsequentialOutputStamper()
    val inconsequentialStamp = ValueOutputStamp(null, instance)
  }

  override fun <O : Out> stamp(output: O): OutputStamp {
    return inconsequentialStamp
  }

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}

data class ValueOutputStamp<out V : Out>(val value: V, override val stamper: OutputStamper) : OutputStamp {
  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(javaClass != other?.javaClass) return false

    other as ValueOutputStamp<*>

    if(value is OutTransientEquatable<*, *> && other.value is OutTransientEquatable<*, *>) {
      if(value.e != other.value.e) {
        return false
      }
    } else if(value != other.value) {
      return false
    }
    if(stamper != other.stamper) return false

    return true
  }

  override fun hashCode(): Int {
    var result = 0
    if(value is OutTransientEquatable<*, *>) {
      result = 31 * result + (value.e?.hashCode() ?: 0)
    } else {
      result = 31 * result + (value?.hashCode() ?: 0)
    }
    result = 31 * result + stamper.hashCode()
    return result
  }
}