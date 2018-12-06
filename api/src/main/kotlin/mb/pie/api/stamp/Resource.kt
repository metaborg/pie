package mb.pie.api.stamp

import mb.pie.api.Resource
import java.io.Serializable

/**
 * Stamper for customizable change detection on resources. Stampers must be [Serializable].
 */
interface ResourceStamper<R: Resource> : Serializable {
  fun stamp(resource: R): ResourceStamp<R>
}

/**
 * Stamp produced by a [ResourceStamper]. Stamps must be [Serializable].
 */
interface ResourceStamp<R: Resource> : Serializable {
  val stamper: ResourceStamper<R>
}
