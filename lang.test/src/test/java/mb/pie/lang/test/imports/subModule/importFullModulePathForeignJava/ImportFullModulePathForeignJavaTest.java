package mb.pie.lang.test.imports.subModule.importFullModulePathForeignJava;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullModulePathForeignJavaTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullModulePathForeignJavaComponent.class, None.instance);
    }
}
