package mb.fs.api.path.match;

import mb.fs.api.path.FSPath;

import java.util.ArrayList;

public class PatternsPathMatcher implements FSPathMatcher {
    private static final long serialVersionUID = 1L;

    private final ArrayList<AntPattern> patterns;


    public PatternsPathMatcher(Iterable<String> patterns) {
        this.patterns = new ArrayList<>();
        for(String pattern : patterns) {
            this.patterns.add(new AntPattern(pattern));
        }
    }


    @Override public boolean matches(FSPath path, FSPath rootDir) {
        final String relative = rootDir.relativize(path).toString();
        for(AntPattern pattern : patterns) {
            if(pattern.match(relative)) {
                return true;
            }
        }
        return false;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final PatternsPathMatcher that = (PatternsPathMatcher) o;
        return patterns.equals(that.patterns);
    }

    @Override public int hashCode() {
        return patterns.hashCode();
    }

    @Override public String toString() {
        return "PatternsPathMatcher(" + patterns + ")";
    }
}
