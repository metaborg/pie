package mb.pie.lang.test.imports.subModule.importFullyQualifiedData;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullyQualifiedDataTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullyQualifiedDataComponent.class, None.instance);
    }
}
