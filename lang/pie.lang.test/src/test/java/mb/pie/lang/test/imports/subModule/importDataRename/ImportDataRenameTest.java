package mb.pie.lang.test.imports.subModule.importDataRename;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportDataRenameTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportDataRenameComponent.class, None.instance);
    }
}
