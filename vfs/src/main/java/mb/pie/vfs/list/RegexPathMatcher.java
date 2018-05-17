package mb.pie.vfs.list;

import mb.pie.vfs.path.PPath;

import java.util.regex.Pattern;

public class RegexPathMatcher implements PathMatcher {
    private static final long serialVersionUID = 2L;

    private final String pattern;
    private transient Pattern compiledPattern;


    public RegexPathMatcher(String pattern) {
        this.pattern = pattern;
        this.compiledPattern = Pattern.compile(pattern);
    }


    @Override public boolean matches(PPath path, PPath root) {
        final String relative = root.normalized().relativizeStringFrom(path.normalized());
        return compiledPattern().matcher(relative).matches();
    }


    private Pattern compiledPattern() {
        if(compiledPattern == null) {
            compiledPattern = Pattern.compile(pattern);
        }
        return compiledPattern;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pattern.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final RegexPathMatcher other = (RegexPathMatcher) obj;
        if(!pattern.equals(other.pattern))
            return false;
        return true;
    }

    @Override public String toString() {
        return "RegexPathMatcher";
    }
}
