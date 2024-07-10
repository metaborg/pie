package mb.pie.lang.test.imports.subModule.importFunctionPseudoModule;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFunctionPseudoModuleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFunctionPseudoModuleComponent.class, None.instance);
    }
}
