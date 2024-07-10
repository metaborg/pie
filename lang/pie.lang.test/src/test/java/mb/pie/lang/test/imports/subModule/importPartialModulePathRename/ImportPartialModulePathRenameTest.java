package mb.pie.lang.test.imports.subModule.importPartialModulePathRename;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportPartialModulePathRenameTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportPartialModulePathRenameComponent.class, None.instance);
    }
}
