package mb.pie.lang.test.path.list.listWithPatterns;

import java.util.HashSet;
import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

class listWithPatternsTest extends PathTestBase {
    @Test void test() throws ExecException {
        HashSet<FSPath> expected = fsPathsOf(
            "A.txt",
            "C.txt",
            "D.txt",
            "E.jav"
        );
        assertOutputEquals(DaggerlistWithPatternsComponent.class, expected);
    }
}
