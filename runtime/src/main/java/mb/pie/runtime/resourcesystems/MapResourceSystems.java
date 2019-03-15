package mb.pie.runtime.resourcesystems;

import mb.pie.api.ResourceSystem;
import mb.pie.api.ResourceSystems;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;

/**
 * Resource systems from a map.
 */
public class MapResourceSystems implements ResourceSystems {
    private final HashMap<String, ResourceSystem> resourceSystems;

    public MapResourceSystems() {
        this.resourceSystems = new HashMap<>();
    }

    public MapResourceSystems(HashMap<String, ResourceSystem> resourceSystems) {
        this.resourceSystems = resourceSystems;
    }


    @Override public @Nullable ResourceSystem getResourceSystem(String id) {
        return resourceSystems.get(id);
    }


    public void add(String id, ResourceSystem resourceSystem) {
        resourceSystems.put(id, resourceSystem);
    }

    public void remove(String id) {
        resourceSystems.remove(id);
    }
}
