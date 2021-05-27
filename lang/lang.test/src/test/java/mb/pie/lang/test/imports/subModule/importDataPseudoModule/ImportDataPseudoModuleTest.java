package mb.pie.lang.test.imports.subModule.importDataPseudoModule;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportDataPseudoModuleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportDataPseudoModuleComponent.class, None.instance);
    }
}
