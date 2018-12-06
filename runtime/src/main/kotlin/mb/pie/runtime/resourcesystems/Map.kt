package mb.pie.runtime.resourcesystems

import mb.pie.api.ResourceSystem
import mb.pie.api.ResourceSystems

/**
 * Resource systems from an immutable map.
 */
open class MapResourceSystems(private val resourceSystems: Map<String, ResourceSystem>) : ResourceSystems {
  override fun getResourceSystem(id: String): ResourceSystem? {
    return resourceSystems[id]
  }
}
