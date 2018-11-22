package mb.fs.api.node.walk;

import mb.fs.api.node.FSNode;

import java.io.IOException;
import java.io.Serializable;

@FunctionalInterface
public interface FSNodeWalker extends Serializable {
    boolean traverse(FSNode dir, FSNode rootDir) throws IOException;
}
