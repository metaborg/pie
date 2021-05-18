package mb.pie.lang.test.imports.subModule.importData;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportDataTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportDataComponent.class, None.instance);
    }
}
