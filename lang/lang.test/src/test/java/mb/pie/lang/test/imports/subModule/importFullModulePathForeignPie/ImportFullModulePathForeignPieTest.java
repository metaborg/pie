package mb.pie.lang.test.imports.subModule.importFullModulePathForeignPie;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullModulePathForeignPieTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullModulePathForeignPieComponent.class, None.instance);
    }
}
