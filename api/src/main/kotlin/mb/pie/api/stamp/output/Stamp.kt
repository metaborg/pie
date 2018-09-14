package mb.pie.api.stamp.output

import mb.pie.api.*
import mb.pie.api.stamp.OutputStamp
import mb.pie.api.stamp.OutputStamper

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

  override fun toString(): String {
    return "Equals"
  }
}

data class ValueOutputStamp<out V : Out>(private val value: V, override val stamper: OutputStamper) : OutputStamp {
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

  override fun toString(): String {
    return "$stamper(${value.toString().toShortString(100)})"
  }
}

class FuncEqualsOutputStamper(private val func: (Out) -> Out) : OutputStamper {
  override fun <O : Out> stamp(output: O): OutputStamp {
    val value = func(output)
    return ValueOutputStamp(value, this)
  }
}

class InconsequentialOutputStamper : OutputStamper {
  companion object {
    val instance = InconsequentialOutputStamper()
  }

  override fun <O : Out> stamp(output: O): OutputStamp {
    return InconsequentialStamp.instance
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
    return "Inconsequential";
  }
}

class InconsequentialStamp : OutputStamp {
  companion object {
    val instance = InconsequentialStamp()
  }

  override val stamper: OutputStamper = InconsequentialOutputStamper.instance

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return "InconsequentialStamp"
  }
}
