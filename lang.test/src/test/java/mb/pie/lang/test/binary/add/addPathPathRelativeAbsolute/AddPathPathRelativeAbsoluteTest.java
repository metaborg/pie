package mb.pie.lang.test.binary.add.addPathPathRelativeAbsolute;

import mb.pie.api.ExecException;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddPathPathRelativeAbsoluteTest {
    @Test void test() throws ExecException {
        FSPath expected = new FSPath("/path/to/foo");
        assertThrows(ExecException.class, () -> {
            assertTaskOutputEquals(DaggeraddPathPathRelativeAbsoluteComponent.class, expected);
        });
    }
}
