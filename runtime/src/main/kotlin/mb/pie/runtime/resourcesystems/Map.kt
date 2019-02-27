package mb.pie.runtime.resourcesystems

import mb.pie.api.ResourceSystem
import mb.pie.api.ResourceSystems

/**
 * Resource systems from an immutable map.
 */
public open class MapResourceSystems : ResourceSystems {
  private val resourceSystems: Map<String, ResourceSystem>;

  constructor(resourceSystems: Map<String, ResourceSystem>) {
    this.resourceSystems = resourceSystems;
  }

  override fun getResourceSystem(id: String): ResourceSystem? {
    return resourceSystems.get(id);
  }
}
