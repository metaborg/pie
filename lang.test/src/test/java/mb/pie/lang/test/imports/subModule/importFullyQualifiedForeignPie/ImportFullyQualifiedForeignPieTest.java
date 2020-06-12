package mb.pie.lang.test.imports.subModule.importFullyQualifiedForeignPie;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullyQualifiedForeignPieTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullyQualifiedForeignPieComponent.class, None.instance);
    }
}
