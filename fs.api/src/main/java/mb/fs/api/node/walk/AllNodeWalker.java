package mb.fs.api.node.walk;

import mb.fs.api.node.FSNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AllNodeWalker implements FSNodeWalker {
    public static final AllNodeWalker instance = new AllNodeWalker();


    @Override public boolean traverse(FSNode dir, FSNode rootDir) {
        return true;
    }


    @Override public boolean equals(@Nullable Object o) {
        return this == o || (o != null && getClass() == o.getClass());
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "AllNodeWalker()";
    }
}
