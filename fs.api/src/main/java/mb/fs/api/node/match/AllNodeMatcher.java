package mb.fs.api.node.match;

import mb.fs.api.node.FSNode;

public class AllNodeMatcher implements FSNodeMatcher {
    private static final long serialVersionUID = 1L;

    public static final AllNodeMatcher instance = new AllNodeMatcher();


    @Override public boolean matches(FSNode node, FSNode rootDir) {
        return true;
    }


    @Override public boolean equals(Object o) {
        return this == o || (o != null && getClass() == o.getClass());
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "AllNodeMatcher";
    }
}
