package mb.fs.api.path.match;

import mb.fs.api.path.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.regex.Pattern;

public class RegexPathMatcher implements FSPathMatcher {
    private final String pattern;
    private transient Pattern compiledPattern;


    public RegexPathMatcher(String pattern) {
        this.pattern = pattern;
        this.compiledPattern = Pattern.compile(pattern);
    }


    @Override public boolean matches(FSPath path, FSPath rootDir) {
        final String relative = rootDir.relativize(path).toString();
        return compiledPattern.matcher(relative).matches();
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final RegexPathMatcher that = (RegexPathMatcher) o;
        return pattern.equals(that.pattern);
    }

    @Override public int hashCode() {
        return pattern.hashCode();
    }

    @Override public String toString() {
        return "RegexPathMatcher(" + pattern + ")";
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        compiledPattern = Pattern.compile(pattern);
    }
}
