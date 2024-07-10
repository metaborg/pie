package mb.pie.task.archive;

import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class ArchiveDirectory implements Serializable {
    final ResourcePath directory;
    final ResourceWalker walker;
    final ResourceMatcher matcher;

    public ArchiveDirectory(ResourcePath directory, ResourceWalker walker, ResourceMatcher matcher) {
        this.directory = directory;
        this.matcher = matcher;
        this.walker = walker;
    }

    public static ArchiveDirectory ofDirectory(ResourcePath directory, ResourceWalker walker, ResourceMatcher matcher) {
        return new ArchiveDirectory(directory, walker, matcher);
    }

    public static ArchiveDirectory ofDirectory(ResourcePath directory, ResourceWalker walker) {
        return ofDirectory(directory, walker, ResourceMatcher.ofTrue());
    }

    public static ArchiveDirectory ofDirectory(ResourcePath directory, ResourceMatcher matcher) {
        return ofDirectory(directory, ResourceWalker.ofTrue(), matcher);
    }

    public static ArchiveDirectory ofDirectory(ResourcePath directory) {
        return ofDirectory(directory, ResourceMatcher.ofTrue());
    }

    public static ArchiveDirectory ofClassFilesInDirectory(ResourcePath directory) {
        return ofDirectory(directory, ResourceMatcher.ofDirectory().or(ResourceMatcher.ofPath(PathMatcher.ofExtension("class"))));
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ArchiveDirectory that = (ArchiveDirectory)o;
        return directory.equals(that.directory) &&
            walker.equals(that.walker) &&
            matcher.equals(that.matcher);
    }

    @Override public int hashCode() {
        return Objects.hash(directory, walker, matcher);
    }

    @Override public String toString() {
        return "ArchiveDirectory{" +
            "directory=" + directory +
            ", walker=" + walker +
            ", matcher=" + matcher +
            '}';
    }
}
