package mb.fs.api.node.walk;

import mb.fs.api.node.FSNode;

public class AllNodeWalker implements FSNodeWalker {
    private static final long serialVersionUID = 1L;

    public static final AllNodeWalker instance = new AllNodeWalker();


    @Override public boolean traverse(FSNode dir, FSNode rootDir) {
        return true;
    }


    @Override public boolean equals(Object o) {
        return this == o || (o != null && getClass() == o.getClass());
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "AllNodeWalker";
    }
}
