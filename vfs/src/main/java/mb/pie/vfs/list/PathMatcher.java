package mb.pie.vfs.list;

import mb.pie.vfs.path.PPath;

import java.io.IOException;
import java.io.Serializable;
import java.util.stream.Stream;

@FunctionalInterface
public interface PathMatcher extends Serializable {
    boolean matches(PPath path, PPath root);


    default Stream<PPath> list(PPath path) throws IOException {
        return path.list(this);
    }
}
