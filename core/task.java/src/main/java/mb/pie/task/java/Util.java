package mb.pie.task.java;

import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.tools.JavaFileObject;

class Util {
    static String qualifiedNameToRelativePath(String qualifiedName) {
        return qualifiedName.replace('.', '/');
    }

    static String relativePathToQualifiedName(String relativePath) {
        return relativePath.replace('/', '.');
    }

    static String kindToExtension(JavaFileObject.Kind kind) {
        return kind.extension.replace(".", "");
    }

    static JavaFileObject.Kind kindOfResource(HierarchicalResource resource) {
        final @Nullable String extension = resource.getLeafExtension();
        if(extension == null) {
            return JavaFileObject.Kind.OTHER;
        }
        if(extension.equals(kindToExtension(JavaFileObject.Kind.SOURCE))) {
            return JavaFileObject.Kind.SOURCE;
        }
        if(extension.equals(kindToExtension(JavaFileObject.Kind.CLASS))) {
            return JavaFileObject.Kind.CLASS;
        }
        if(extension.equals(kindToExtension(JavaFileObject.Kind.HTML))) {
            return JavaFileObject.Kind.HTML;
        }
        return JavaFileObject.Kind.OTHER;
    }
}
