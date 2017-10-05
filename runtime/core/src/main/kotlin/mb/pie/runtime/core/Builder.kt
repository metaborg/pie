package mb.pie.runtime.core

import java.io.Serializable

typealias In = Serializable?
typealias Out = Serializable?


interface OutTransient<out T : Any?> : Out {
  val v: T
  val consistent: Boolean
}

data class OutTransientImpl<out T : Any?>(
  @Transient override val v: T,
  @Transient override val consistent: Boolean = false
) : OutTransient<T> {
  constructor(v: T) : this(v, true)
}


interface OutTransientEquatable<out T : Any?, out E : Out> : OutTransient<T> {
  val e: E
}

data class OutTransientEquatableImpl<out T : Any?, out E : Out>(
  @Transient override val v: T,
  @Transient override val consistent: Boolean = false,
  override val e: E
) : OutTransientEquatable<T, E> {
  constructor(v: T, e: E) : this(v, true, e)
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
typealias AnyBuilder = Builder<In, Out>


class BuildException(message: String, cause: Exception?) : Exception(message, cause) {
  constructor(message: String) : this(message, null)
}
