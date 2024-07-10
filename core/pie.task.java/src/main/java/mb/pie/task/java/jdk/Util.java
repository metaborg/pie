package mb.pie.task.java.jdk;

import mb.resource.hierarchical.FilenameExtensionUtil;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.tools.JavaFileObject.Kind;

class Util {
    static String qualifiedNameToRelativePath(String qualifiedName) {
        return qualifiedName.replace('.', '/');
    }

    static String relativePathToQualifiedName(String relativePath) {
        return relativePath.replace('/', '.');
    }


    static String kindToExtension(Kind kind) {
        return kind.extension.replace(".", "");
    }

    static Kind extensionToKind(@Nullable String extension) {
        if(extension == null) return Kind.OTHER;
        if(extension.equals(kindToExtension(Kind.SOURCE))) return Kind.SOURCE;
        if(extension.equals(kindToExtension(Kind.CLASS))) return Kind.CLASS;
        if(extension.equals(kindToExtension(Kind.HTML))) return Kind.HTML;
        return Kind.OTHER;
    }

    static Kind kindOfResource(HierarchicalResource resource) {
        final @Nullable String extension = resource.getLeafExtension();
        return extensionToKind(extension);
    }

    static Kind kindOfFilename(String filename) {
        final @Nullable String extension = FilenameExtensionUtil.getExtension(filename);
        return extensionToKind(extension);
    }
}
