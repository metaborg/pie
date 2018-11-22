package mb.fs.api.node.match;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.match.FSPathMatcher;

import java.io.IOException;

public class FileNodeMatcher implements FSNodeMatcher {
    private static final long serialVersionUID = 1L;

    public static final FileNodeMatcher instance = new FileNodeMatcher();


    @Override public boolean matches(FSNode node, FSNode rootDir) throws IOException {
        return node.isFile();
    }


    @Override public boolean equals(Object o) {
        return this == o || (o != null && getClass() == o.getClass());
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "FileNodeMatcher";
    }
}
