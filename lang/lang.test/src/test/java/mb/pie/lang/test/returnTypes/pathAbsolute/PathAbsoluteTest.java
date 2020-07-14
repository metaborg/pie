package mb.pie.lang.test.returnTypes.pathAbsolute;

import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class PathAbsoluteTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerpathAbsoluteComponent.class, new FSPath("/foo/bar"));
    }
}
