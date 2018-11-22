package mb.fs.api.node.match;

import mb.fs.api.node.FSNode;
import mb.fs.api.path.match.FSPathMatcher;

public class PathNodeMatcher implements FSNodeMatcher {
    private static final long serialVersionUID = 1L;

    private final FSPathMatcher matcher;


    public PathNodeMatcher(FSPathMatcher matcher) {
        this.matcher = matcher;
    }


    @Override public boolean matches(FSNode node, FSNode rootDir) {
        return matcher.matches(node.getPath(), rootDir.getPath());
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final PathNodeMatcher that = (PathNodeMatcher) o;
        return matcher.equals(that.matcher);
    }

    @Override public int hashCode() {
        return matcher.hashCode();
    }

    @Override public String toString() {
        return "PathNodeMatcher(" + matcher + ")";
    }
}
