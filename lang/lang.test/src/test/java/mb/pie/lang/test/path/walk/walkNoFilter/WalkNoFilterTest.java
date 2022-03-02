package mb.pie.lang.test.path.walk.walkNoFilter;

import java.util.HashSet;
import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class WalkNoFilterTest extends PathTestBase {
    @Test void test() throws ExecException {
        HashSet<FSPath> expected = fsPathsOf(
            "results",
            "results/result_01X3.csv",
            "results/result_02G3.csv",
            "results/result_02G5.csv",
            "results/result_02X3.csv",
            "results/result_03X3.csv",
            "A.txt",
            "B.tig",
            "C.txt",
            "D.txt",
            "E.jav"
        );
        expected.add(SAMPLE_DIR);
        assertOutputEquals(DaggerwalkNoFilterComponent.class, expected);
    }
}
