package mb.pie.lang.test.binary.add.addStrPath;

import mb.pie.api.ExecException;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrPathTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrPathComponent.class, "String + path: " + new FSPath("/path/to/file"));
    }
}
