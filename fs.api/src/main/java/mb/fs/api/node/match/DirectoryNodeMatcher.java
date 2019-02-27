package mb.fs.api.node.match;

import mb.fs.api.node.FSNode;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

public class DirectoryNodeMatcher implements FSNodeMatcher {
    public static final DirectoryNodeMatcher instance = new DirectoryNodeMatcher();


    @Override public boolean matches(FSNode node, FSNode rootDir) throws IOException {
        return node.isDirectory();
    }


    @Override public boolean equals(@Nullable Object o) {
        return this == o || (o != null && getClass() == o.getClass());
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "DirectoryNodeMatcher()";
    }
}
