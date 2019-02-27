package mb.fs.api.node.match;

import mb.fs.api.node.FSNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AllNodeMatcher implements FSNodeMatcher {
    public static final AllNodeMatcher instance = new AllNodeMatcher();


    @Override public boolean matches(FSNode node, FSNode rootDir) {
        return true;
    }


    @Override public boolean equals(@Nullable Object o) {
        return this == o || (o != null && getClass() == o.getClass());
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "AllNodeMatcher()";
    }
}
