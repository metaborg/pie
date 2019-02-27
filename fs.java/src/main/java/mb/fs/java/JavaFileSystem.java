package mb.fs.java;

import mb.fs.api.FileSystem;
import mb.fs.api.path.FSPath;
import mb.fs.api.path.InvalidFSPathRuntimeException;

import java.io.Serializable;

public class JavaFileSystem implements FileSystem, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String id = "java";
    public static JavaFileSystem instance = new JavaFileSystem();

    private JavaFileSystem() {}

    @Override public String getId() {
        return id;
    }

    @Override public JavaFSNode getNode(FSPath path) {
        if(!(path instanceof JavaFSPath)) {
            throw new InvalidFSPathRuntimeException(
                "Cannot get file system node for path " + path + ", it is not a Java file system path");
        }
        final JavaFSPath javaFsPath = (JavaFSPath) path;
        return new JavaFSNode(javaFsPath);
    }

    public JavaFSNode getNode(JavaFSPath path) {
        return new JavaFSNode(path);
    }

    @Override public void close() {
        // Nothing to close.
    }
}
