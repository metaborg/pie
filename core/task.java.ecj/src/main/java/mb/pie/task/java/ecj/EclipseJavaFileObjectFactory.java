package mb.pie.task.java.ecj;

import mb.pie.task.java.JavaFileObjectFactory;
import mb.resource.hierarchical.HierarchicalResource;

import javax.tools.JavaFileObject;

public class EclipseJavaFileObjectFactory implements JavaFileObjectFactory {
    @Override public JavaFileObject create(HierarchicalResource resource) {
        return new EclipseJavaResource(resource);
    }

    @Override public JavaFileObject create(HierarchicalResource resource, JavaFileObject.Kind kind) {
        return new EclipseJavaResource(resource, kind);
    }
}
