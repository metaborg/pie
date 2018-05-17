package mb.pie.vfs.path;

import mb.pie.vfs.list.*;

import java.util.Collection;

public class PPaths {
    public static PathMatcher allPathMatcher() {
        return new AllPathMatcher();
    }

    public static PathWalker allPathWalker() {
        return new AllPathWalker();
    }


    public static PathMatcher extensionsPathMatcher(String extension) {
        return new ExtensionsPathMatcher(extension);
    }

    public static PathMatcher extensionsPathMatcher(Collection<String> extensions) {
        return new ExtensionsPathMatcher(extensions);
    }

    public static PathWalker extensionsPathWalker(String extension) {
        return new PathMatcherWalker(extensionsPathMatcher(extension));
    }

    public static PathWalker extensionsPathWalker(Collection<String> extensions) {
        return new PathMatcherWalker(extensionsPathMatcher(extensions));
    }


    public static PathMatcher patternsPathMatcher(Collection<String> patterns) {
        return new PatternsPathMatcher(patterns);
    }

    public static PathMatcher patternsPathMatcher(String pattern) {
        return new PatternsPathMatcher(pattern);
    }

    public static PathWalker patternsPathWalker(Collection<String> patterns) {
        return new PathMatcherWalker(patternsPathMatcher(patterns));
    }

    public static PathWalker patternsPathWalker(String pattern) {
        return new PathMatcherWalker(patternsPathMatcher(pattern));
    }


    public static PathMatcher regexPathMatcher(String regex) {
        return new RegexPathMatcher(regex);
    }

    public static PathWalker regexPathWalker(String regex) {
        return new PathMatcherWalker(regexPathMatcher(regex));
    }


    public static PathMatcher directoryPathMatcher() {
        // TODO: make ignoring hidden directories configurable
        return new DirectoryPathMatcher(true);
    }

    public static PathWalker directoryPathWalker() {
        // TODO: make ignoring hidden directories configurable
        return new DirectoryPathWalker(true);
    }
}
