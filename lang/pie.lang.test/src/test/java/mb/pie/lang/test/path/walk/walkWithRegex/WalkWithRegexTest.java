package mb.pie.lang.test.path.walk.walkWithRegex;

import java.util.HashSet;
import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

class walkWithRegexTest extends PathTestBase {
    @Test void test() throws ExecException {
        HashSet<FSPath> expected = fsPathsOf(
            "results/result_01X3.csv",
            "results/result_02X3.csv",
            "results/result_03X3.csv"
        );
        assertOutputEquals(DaggerwalkWithRegexComponent.class, expected);
    }
}
