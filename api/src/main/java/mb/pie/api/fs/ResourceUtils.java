package mb.pie.api.fs;

import mb.fs.api.FileSystem;
import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;
import mb.fs.java.JavaFSNode;
import mb.fs.java.JavaFSPath;
import mb.fs.java.JavaFileSystem;
import mb.pie.api.ResourceKey;
import mb.pie.api.ResourceSystem;
import mb.pie.api.ResourceSystems;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.file.Path;

public class ResourceUtils {
    public static ResourceKey toResourceKey(FSPath path) {
        return new ResourceKey(path.getFileSystemId(), path);
    }

    public static ResourceKey toResourceKey(FSNode node) {
        return new ResourceKey(node.getFileSystemId(), node.getPath());
    }

    public static ResourceKey toResourceKey(JavaFSPath javaFSPath) {
        return new ResourceKey(JavaFileSystem.id, javaFSPath);
    }

    public static ResourceKey toResourceKey(JavaFSNode node) {
        return new ResourceKey(JavaFileSystem.id, node.getPath());
    }

    public static ResourceKey toResourceKey(Path path) {
        return new ResourceKey(JavaFileSystem.id, new JavaFSPath(path));
    }

    public static ResourceKey toResourceKey(File file) {
        return new ResourceKey(JavaFileSystem.id, new JavaFSPath(file));
    }


    public static FileSystemResource toResource(FSPath path, ResourceSystems resourceSystems) {
        return new FileSystemResource(toNode(path, resourceSystems));
    }

    public static FileSystemResource toResource(FSPath path, ResourceSystem resourceSystem) {
        return new FileSystemResource(toNode(path, resourceSystem));
    }

    public static FileSystemResource toResource(FSPath path, FileSystemResourceSystem resourceSystem) {
        return new FileSystemResource(toNode(path, resourceSystem));
    }

    public static FileSystemResource toResource(FSPath path, FileSystem fileSystem) {
        return new FileSystemResource(toNode(path, fileSystem));
    }

    public static FileSystemResource toResource(FSNode node) {
        return new FileSystemResource(node);
    }

    public static FileSystemResource toResource(JavaFSNode javaFSNode) {
        return new FileSystemResource(javaFSNode);
    }

    public static FileSystemResource toResource(JavaFSPath path) {
        return new FileSystemResource(path.toNode());
    }

    public static FileSystemResource toResource(Path path) {
        return new FileSystemResource(toNode(path));
    }

    public static FileSystemResource toResource(File file) {
        return new FileSystemResource(toNode(file));
    }


    public static FSNode toNode(FSPath path, ResourceSystems resourceSystems) {
        final String fileSystemId = path.getFileSystemId();
        final @Nullable ResourceSystem resourceSystem = resourceSystems.getResourceSystem(fileSystemId);
        if(resourceSystem == null) {
            throw new RuntimeException(
                "Cannot convert path '" + path + "' to a file system node; resource system with id '" + fileSystemId + "' could not be found");
        }
        if(!(resourceSystem instanceof FileSystemResourceSystem)) {
            throw new RuntimeException(
                "Cannot convert path '" + path + "' to a file system node; resource system with id '" + fileSystemId + "' is not a FileSystemResourceSystem");
        }
        return toNode(path, ((FileSystemResourceSystem) resourceSystem).fileSystem);
    }

    public static FSNode toNode(FSPath path, ResourceSystem resourceSystem) {
        if(!(resourceSystem instanceof FileSystemResourceSystem)) {
            throw new RuntimeException(
                "Cannot convert path '" + path + "' to a file system node; resource system '" + resourceSystem + "' is not a FileSystemResourceSystem");
        }
        return toNode(path, ((FileSystemResourceSystem) resourceSystem).fileSystem);
    }

    public static FSNode toNode(FSPath path, FileSystemResourceSystem resourceSystem) {
        return toNode(path, resourceSystem.fileSystem);
    }

    public static FSNode toNode(FSPath path, FileSystem fileSystem) {
        return fileSystem.getNode(path);
    }

    public static JavaFSNode toNode(Path path) {
        return new JavaFSNode(path);
    }

    public static JavaFSNode toNode(File file) {
        return new JavaFSNode(file);
    }
}
