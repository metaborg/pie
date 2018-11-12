package mb.fs.java;

import java.nio.file.Path;
import java.util.Iterator;

public class PathIterator implements Iterator<String> {
    private final Iterator<Path> iterator;

    public PathIterator(Iterator<Path> iterator) {
        this.iterator = iterator;
    }

    @Override public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override public String next() {
        return iterator.next().toString();
    }
}
