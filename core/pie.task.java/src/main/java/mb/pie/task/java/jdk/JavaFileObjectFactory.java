package mb.pie.task.java.jdk;

import mb.resource.hierarchical.HierarchicalResource;

import javax.tools.JavaFileObject;

public interface JavaFileObjectFactory {
    JavaFileObject create(HierarchicalResource resource);

    JavaFileObject create(HierarchicalResource resource, JavaFileObject.Kind kind);
}
