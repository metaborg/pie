package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class PathRelativeTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_pathRelative(), main_pathRelative.class, new FSPath("./path/to/foo"));
    }
}
