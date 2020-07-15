package mb.pie.lang.test.returnTypes.pathRelative;

import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class PathRelativeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerpathRelativeComponent.class, new FSPath("./path/to/foo"));
    }
}
