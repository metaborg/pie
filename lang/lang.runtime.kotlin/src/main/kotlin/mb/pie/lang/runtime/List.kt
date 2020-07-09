package mb.pie.lang.runtime

import java.io.Serializable
import java.util.*

fun <T : Serializable?> list(vararg elements: T): ArrayList<T> {
  val list = ArrayList<T>()
  list.addAll(elements)
  return list
}

operator fun <T : Serializable?> ArrayList<T>.plus(other: T): ArrayList<T> {
  val list = ArrayList<T>(this)
  list.add(other)
  return list
}

operator fun <T : Serializable?> ArrayList<T>.plus(other: ArrayList<T>): ArrayList<T> {
  val list = ArrayList<T>(this)
  list.addAll(other)
  return list
}
