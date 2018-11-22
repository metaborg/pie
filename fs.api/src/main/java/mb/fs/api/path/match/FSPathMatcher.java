package mb.fs.api.path.match;

import mb.fs.api.path.FSPath;

import java.io.Serializable;

@FunctionalInterface
public interface FSPathMatcher extends Serializable {
    boolean matches(FSPath path, FSPath rootDir);
}
