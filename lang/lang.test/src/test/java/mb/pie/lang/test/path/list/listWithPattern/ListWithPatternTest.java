package mb.pie.lang.test.path.list.listWithPattern;

import java.util.HashSet;
import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

class listWithPatternTest extends PathTestBase {
    @Test void test() throws ExecException {
        HashSet<FSPath> expected = fsPathsOf(
            "A.txt",
            "B.tig",
            "C.txt",
            "D.txt"
        );
        assertOutputEquals(DaggerlistWithPatternComponent.class, expected);
    }
}
