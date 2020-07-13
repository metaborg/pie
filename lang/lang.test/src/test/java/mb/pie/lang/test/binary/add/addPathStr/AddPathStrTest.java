package mb.pie.lang.test.binary.add.addPathStr;

import mb.pie.api.ExecException;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddPathStrTest {
    @Test void test() throws ExecException {
        FSPath expected = new FSPath("/path/to/foo/bar");
        assertTaskOutputEquals(DaggeraddPathStrComponent.class, expected);
    }
}
