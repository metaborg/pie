package mb.fs.api;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;

import java.io.Closeable;

public interface FileSystem extends Closeable {
    String getRootSelector();

    boolean isValidPath(FSPath path);

    FSNode getNode(FSPath path);
}
