package mb.pie.lang.test.imports.subModule.importFullyQualified;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullyQualifiedTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullyQualifiedComponent.class, None.instance);
    }
}
