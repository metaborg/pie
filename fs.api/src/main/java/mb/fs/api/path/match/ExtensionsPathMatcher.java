package mb.fs.api.path.match;

import mb.fs.api.path.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class ExtensionsPathMatcher implements FSPathMatcher {
    private final List<String> extensions;
    private transient HashSet<String> extensionsHashSet;


    public ExtensionsPathMatcher(Collection<String> extensions) {
        this.extensions = new ArrayList<>(extensions);
        this.extensionsHashSet = new HashSet<>(extensions);
    }

    public ExtensionsPathMatcher(String... extensions) {
        this.extensions = Arrays.asList(extensions);
        this.extensionsHashSet = new HashSet<>(this.extensions);
    }


    @Override public boolean matches(FSPath path, FSPath rootDir) {
        final @Nullable String extension = path.getLeafExtension();
        if(extension == null) {
            return false;
        }
        return extensionsHashSet.contains(extension);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ExtensionsPathMatcher that = (ExtensionsPathMatcher) o;
        return extensions.equals(that.extensions);
    }

    @Override public int hashCode() {
        return extensions.hashCode();
    }

    @Override public String toString() {
        return "ExtensionsPathMatcher(" + extensions + ")";
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        extensionsHashSet = new HashSet<>(extensions);
    }
}
