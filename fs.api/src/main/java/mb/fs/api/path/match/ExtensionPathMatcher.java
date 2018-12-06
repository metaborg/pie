package mb.fs.api.path.match;

import mb.fs.api.path.FSPath;

import javax.annotation.Nullable;

public class ExtensionPathMatcher implements FSPathMatcher {
    private static final long serialVersionUID = 1L;

    private final String extension;


    public ExtensionPathMatcher(String extension) {
        this.extension = extension;
    }


    @Override public boolean matches(FSPath path, FSPath rootDir) {
        final @Nullable String extension = path.getLeafExtension();
        if(extension == null) {
            return false;
        }
        return this.extension.equals(extension);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ExtensionPathMatcher that = (ExtensionPathMatcher) o;
        return extension.equals(that.extension);
    }

    @Override public int hashCode() {
        return extension.hashCode();
    }

    @Override public String toString() {
        return "ExtensionPathMatcher(" + extension + ")";
    }
}
