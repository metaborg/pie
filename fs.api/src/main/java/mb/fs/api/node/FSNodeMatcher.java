package mb.fs.api.node;

import java.io.Serializable;

@FunctionalInterface
public interface FSNodeMatcher extends Serializable {
    boolean matches(FSNode node, FSNode rootDir);
}
