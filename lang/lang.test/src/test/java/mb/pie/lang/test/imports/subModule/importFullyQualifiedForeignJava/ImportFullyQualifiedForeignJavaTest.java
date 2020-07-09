package mb.pie.lang.test.imports.subModule.importFullyQualifiedForeignJava;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullyQualifiedForeignJavaTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullyQualifiedForeignJavaComponent.class, None.instance);
    }
}
