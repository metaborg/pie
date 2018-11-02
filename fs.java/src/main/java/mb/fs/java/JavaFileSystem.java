package mb.fs.java;

import mb.fs.api.FileSystem;
import mb.fs.api.path.FSPath;

public class JavaFileSystem implements FileSystem {
    public static final String rootSelector = "java";

    @Override public String getRootSelector() {
        return rootSelector;
    }

    @Override public boolean isValidPath(FSPath path) {
        return path.getSelectorRoot().equals(rootSelector);
    }

    @Override public JavaFSNode getNode(FSPath path) {
        return new JavaFSNode(path);
    }

    @Override public void close() {
        // Nothing to close.
    }
}
