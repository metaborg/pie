package mb.pie.lang.test.binary.add.addPathPathRelativeRelative;

import mb.pie.api.ExecException;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddPathPathRelativeRelativeTest {
    @Test void test() throws ExecException {
        FSPath expected = new FSPath("./path/to/./foo");
        assertTaskOutputEquals(DaggeraddPathPathRelativeRelativeComponent.class, expected);
    }
}
