package mb.pie.vfs.list;

import mb.pie.vfs.path.PPath;

import java.util.*;

public class ExtensionsPathMatcher implements PathMatcher {
    private static final long serialVersionUID = 1L;

    private final ArrayList<String> extensions;
    private transient HashSet<String> extensionsHashSet;


    public ExtensionsPathMatcher(Collection<String> extensions) {
        this.extensions = new ArrayList<>(extensions);
        this.extensionsHashSet = new HashSet<>(extensions);
    }

    public ExtensionsPathMatcher(String extension) {
        this.extensions = new ArrayList<>();
        this.extensions.add(extension);
        this.extensionsHashSet = new HashSet<>(this.extensions);
    }


    @Override public boolean matches(PPath path, PPath root) {
        if(!path.isFile()) {
            return false;
        }
        final String extension = path.extension();
        if(extension == null) {
            return false;
        }
        if(extensionsHashSet == null) {
            extensionsHashSet = new HashSet<>(extensions);
        }
        return extensionsHashSet.contains(extension);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + extensions.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ExtensionsPathMatcher other = (ExtensionsPathMatcher) obj;
        if(!extensions.equals(other.extensions))
            return false;
        return true;
    }

    @Override public String toString() {
        return "ExtensionsPathMatcher(" + String.join(", ", extensions) + ")";
    }
}
