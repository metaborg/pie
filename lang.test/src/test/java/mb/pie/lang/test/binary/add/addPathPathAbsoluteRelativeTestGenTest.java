package mb.pie.lang.test.binary.add;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class addPathPathAbsoluteRelativeTestGenTest {
    @Test void test() throws ExecException {
        FSPath expected = new FSPath("/path/to/./foo");
        assertTaskOutputEquals(new TaskDefsModule_addPathPathAbsoluteRelativeTestGen(), main_addPathPathAbsoluteRelativeTestGen.class, expected);
    }
}
