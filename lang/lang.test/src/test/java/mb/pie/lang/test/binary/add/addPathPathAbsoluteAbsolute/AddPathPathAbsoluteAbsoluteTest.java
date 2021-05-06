package mb.pie.lang.test.binary.add.addPathPathAbsoluteAbsolute;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static mb.pie.lang.test.util.SimpleChecker.requireTask;
import static org.junit.jupiter.api.Assertions.*;

class AddPathPathAbsoluteAbsoluteTest {
    @Test void test() throws ExecException {
        if("/".equals(File.separator)) {
            // TODO: fix this test being skipped on Windows, because it behaves differently there due to file separators.
            assertThrows(ExecException.class, () -> requireTask(DaggeraddPathPathAbsoluteAbsoluteComponent.class));
        }
    }
}
