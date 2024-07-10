package mb.pie.lang.test.path.list.listNoFilter;

import java.util.HashSet;
import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListNoFilterTest extends PathTestBase {
    @Test void test() throws ExecException {
        HashSet<FSPath> expected = fsPathsOf(
            "results",
            "A.txt",
            "B.tig",
            "C.txt",
            "D.txt",
            "E.jav"
        );
        assertOutputEquals(DaggerlistNoFilterComponent.class, expected);
    }
}
