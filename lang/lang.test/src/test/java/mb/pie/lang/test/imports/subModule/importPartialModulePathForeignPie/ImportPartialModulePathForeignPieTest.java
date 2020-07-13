package mb.pie.lang.test.imports.subModule.importPartialModulePathForeignPie;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportPartialModulePathForeignPieTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportPartialModulePathForeignPieComponent.class, None.instance);
    }
}
