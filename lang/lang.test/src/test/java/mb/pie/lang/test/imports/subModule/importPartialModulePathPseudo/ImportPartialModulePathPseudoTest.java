package mb.pie.lang.test.imports.subModule.importPartialModulePathPseudo;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportPartialModulePathPseudoTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportPartialModulePathPseudoComponent.class, None.instance);
    }
}
