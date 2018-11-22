package mb.fs.api.node.match;

import mb.fs.api.node.FSNode;

import java.io.IOException;
import java.io.Serializable;

@FunctionalInterface
public interface FSNodeMatcher extends Serializable {
    boolean matches(FSNode node, FSNode rootDir) throws IOException;
}
