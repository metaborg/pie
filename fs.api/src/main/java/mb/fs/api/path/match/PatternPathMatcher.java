package mb.fs.api.path.match;

import mb.fs.api.path.FSPath;

public class PatternPathMatcher implements FSPathMatcher {
    private static final long serialVersionUID = 1L;

    private final AntPattern pattern;


    public PatternPathMatcher(AntPattern pattern) {
        this.pattern = pattern;
    }


    @Override public boolean matches(FSPath path, FSPath rootDir) {
        final String relative = rootDir.relativize(path).toString();
        return pattern.match(relative);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final PatternPathMatcher that = (PatternPathMatcher) o;
        return pattern.equals(that.pattern);
    }

    @Override public int hashCode() {
        return pattern.hashCode();
    }

    @Override public String toString() {
        return "PatternPathMatcher(" + pattern + ")";
    }
}
