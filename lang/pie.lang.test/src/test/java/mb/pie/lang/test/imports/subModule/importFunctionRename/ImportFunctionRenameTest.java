package mb.pie.lang.test.imports.subModule.importFunctionRename;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFunctionRenameTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFunctionRenameComponent.class, None.instance);
    }
}
