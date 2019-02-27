package mb.pie.runtime.resourcesystems

import mb.pie.api.ResourceSystem
import mb.pie.api.ResourceSystems

/**
 * Resource systems from a mutable map.
 */
public open class MutableMapResourceSystems : ResourceSystems {
  private val resourceSystems = mutableMapOf<String, ResourceSystem>()

  override fun getResourceSystem(id: String): ResourceSystem? {
    return resourceSystems.get(id);
  }

  fun add(id: String, resourceSystem: ResourceSystem) {
    resourceSystems.set(id, resourceSystem);
  }

  fun remove(id: String) {
    resourceSystems.remove(id);
  }
}
