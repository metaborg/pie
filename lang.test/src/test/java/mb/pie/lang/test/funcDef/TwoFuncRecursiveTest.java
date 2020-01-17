package mb.pie.lang.test.funcDef;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.runtime.layer.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TwoFuncRecursiveTest {
    @Test @Timeout(5) void test() throws ExecException {
        assertThrows(ValidationException.class, () ->
            assertTaskOutputEquals(new TaskDefsModule_twoFuncRecursiveTestGen(), main_twoFuncRecursive.class, None.instance)
        );
    }
}
