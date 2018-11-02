package mb.fs.api.node;

import java.io.Serializable;

@FunctionalInterface
public interface FSNodeWalker extends Serializable {
    boolean traverse(FSNode dir, FSNode rootDir);
}
