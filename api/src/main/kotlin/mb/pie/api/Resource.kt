package mb.pie.api

import java.io.Serializable

/**
 * Mutable resource with a [key] that uniquely identifies the resource.
 */
interface Resource {
  fun key(): ResourceKey
}

/**
 * Unique key of a resource consisting of the [resource system identifier][id] and [key] uniquely identifying the resource within the
 * resource system.
 */
data class ResourceKey(val id: String, val key: Key): Serializable {
  fun equals(other: ResourceKey): Boolean {
    if(id != other.id) return false
    if(key != other.key) return false
    return true
  }

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(javaClass != other?.javaClass) return false
    return equals(other as ResourceKey)
  }

  @Transient
  private var hashCodeIsCached = false
  @Transient
  private var hashCodeCache: Int = 0

  override fun hashCode(): Int {
    if(!hashCodeIsCached) {
      hashCodeCache = id.hashCode() + 31 * key.hashCode()
      hashCodeIsCached = true
    }
    return hashCodeCache
  }

  @JvmOverloads
  fun toShortString(maxLength: Int = 100) = "#$id:${key.toString().toShortString(maxLength)}"

  override fun toString() = toShortString()
}

/**
 * Resource system with a [unique identifier][id] that [resolves resource keys into resources][getResource].
 */
interface ResourceSystem {
  val id: String
  fun getResource(key: ResourceKey): Resource
}

/**
 * Collection of [resource systems][ResourceSystem].
 */
interface ResourceSystems {
  fun getResourceSystem(id: String): ResourceSystem?
}
