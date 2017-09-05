package mb.pie.runtime.core

import java.io.Serializable

typealias In = Serializable?
typealias Out = Serializable?

data class OutTransient<T : Any?>(@Transient val v: T, @Transient val consistent: Boolean = false) : Out {
  constructor(v: T) : this(v, true)
}


interface Builder<in I : In, out O : Out> {
  @Throws(BuildException::class)
  fun BuildContext.build(input: I): O

  @Throws(BuildException::class)
  fun build(input: I, ctx: BuildContext): O {
    return ctx.build(input)
  }

  fun mayOverlap(input1: I, input2: I): Boolean = input1 == input2

  val id: String
  fun desc(input: I): String = "$id(${input.toString().toShortString(100)})"
}
typealias UBuilder = Builder<*, *>


class BuildException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}
