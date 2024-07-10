package mb.pie.lang.test.imports.subModule.multiImport;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class MultiImportTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggermultiImportComponent.class, false);
    }
}
