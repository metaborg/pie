package mb.pie.lang.test.imports.subModule.importPartialModulePath;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportPartialModulePathTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportPartialModulePathComponent.class, None.instance);
    }
}
