package mb.fs.api;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

public class GeneralFileSystem implements Closeable {
    private final HashMap<String, FileSystem> fileSystemsPerSelector = new HashMap<>();


    public void registerFileSystem(String selector, FileSystem fileSystem) {
        fileSystemsPerSelector.put(selector, fileSystem);
    }


    public boolean isValidPath(FSPath path) {
        final String selectorRoot = path.getSelectorRoot();
        final FileSystem fileSystem = fileSystemsPerSelector.get(selectorRoot);
        if(fileSystem == null) {
            return false;
        }
        return fileSystem.isValidPath(path);
    }

    public FSNode getNode(FSPath path) {
        final String selectorRoot = path.getSelectorRoot();
        final FileSystem fileSystem = fileSystemsPerSelector.get(selectorRoot);
        if(fileSystem == null) {
            throw new IllegalArgumentException(
                "Cannot get resource for path " + path + "; no filesystem for selector '" + selectorRoot + "' was registered");
        }
        return fileSystem.getNode(path);
    }


    @Override public void close() throws IOException {
        for(FileSystem fileSystem : fileSystemsPerSelector.values()) {
            fileSystem.close();
        }
        fileSystemsPerSelector.clear();
    }
}
