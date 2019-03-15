package mb.pie.api.fs;

import mb.fs.api.node.FSNode;
import mb.pie.api.Resource;
import mb.pie.api.ResourceKey;

/**
 * Resource for file system nodes.
 */
public class FileSystemResource implements Resource {
    public final FSNode node;


    public FileSystemResource(FSNode node) {
        this.node = node;
    }


    @Override public ResourceKey getKey() {
        return new ResourceKey(node.getFileSystem().getId(), node.getPath());
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FileSystemResource that = (FileSystemResource) o;
        return node.equals(that.node);
    }

    @Override public int hashCode() {
        return node.hashCode();
    }

    @Override public String toString() {
        return "FileSystemResource(" + node + ')';
    }
}
