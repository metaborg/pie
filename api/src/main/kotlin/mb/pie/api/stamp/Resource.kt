package mb.pie.api.stamp

import mb.pie.api.Resource
import java.io.Serializable

/**
 * Stamper for customizable change detection on resources. Stampers must be [Serializable].
 */
interface ResourceStamper : Serializable {
  fun stamp(resource: Resource): ResourceStamp
}

/**
 * Stamp produced by a [ResourceStamper]. Stamps must be [Serializable].
 */
interface ResourceStamp : Serializable {
  val stamper: ResourceStamper
}
