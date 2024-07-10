package mb.pie.lang.test.imports.subModule.multiImportPseudoModule;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class MultiImportPseudoModuleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggermultiImportPseudoModuleComponent.class, false);
    }
}
