package mb.pie.lang.test.binary.add;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.Session;
import mb.pie.lang.test.util.PieRunner;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class addPathPathRelativeAbsoluteTestGenTest {
    @Test void test() throws ExecException {
        FSPath expected = new FSPath("/path/to/foo");
        assertThrows(ExecException.class, () -> {
            assertTaskOutputEquals(new TaskDefsModule_addPathPathRelativeAbsoluteTestGen(), main_addPathPathRelativeAbsoluteTestGen.class, expected);
        });
    }
}
