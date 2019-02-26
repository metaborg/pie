package mb.pie.api.fs;

import mb.fs.api.FileSystem;
import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;
import mb.pie.api.ResourceKey;
import mb.pie.api.ResourceSystem;

/**
 * Resource system for the general file system.
 */
public class FileSystemResourceSystem implements ResourceSystem {
    public final FileSystem fileSystem;


    public FileSystemResourceSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }


    @Override public String getId() {
        return fileSystem.getId();
    }

    @Override public FileSystemResource getResource(ResourceKey key) {
        if(!key.id.equals(getId())) {
            throw new RuntimeException(
                "Attempting to get resource for key '" + key + "', which is not a file system resource key");
        }
        final FSPath path = (FSPath) key.key;
        final FSNode node = fileSystem.getNode(path);
        return new FileSystemResource(node);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FileSystemResourceSystem that = (FileSystemResourceSystem) o;
        return fileSystem.equals(that.fileSystem);
    }

    @Override public int hashCode() {
        return fileSystem.hashCode();
    }

    @Override public String toString() {
        return "FileSystemResourceSystem(" + fileSystem + ')';
    }
}
