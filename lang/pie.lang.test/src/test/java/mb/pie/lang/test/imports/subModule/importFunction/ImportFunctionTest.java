package mb.pie.lang.test.imports.subModule.importFunction;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFunctionTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFunctionComponent.class, None.instance);
    }
}
