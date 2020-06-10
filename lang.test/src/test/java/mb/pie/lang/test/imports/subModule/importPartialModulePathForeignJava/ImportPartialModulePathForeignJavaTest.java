package mb.pie.lang.test.imports.subModule.importPartialModulePathForeignJava;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportPartialModulePathForeignJavaTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportPartialModulePathForeignJavaComponent.class, None.instance);
    }
}
