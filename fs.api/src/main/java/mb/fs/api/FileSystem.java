package mb.fs.api;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;
import mb.fs.api.path.InvalidFSPathRuntimeException;

import java.io.Closeable;

public interface FileSystem extends Closeable {
    String getId();

    /**
     * @throws InvalidFSPathRuntimeException when path is not of the type this file system supports.
     */
    FSNode getNode(FSPath path);
}
