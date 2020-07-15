package mb.pie.lang.test.imports.subModule.importFullModulePath;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullModulePathTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullModulePathComponent.class, None.instance);
    }
}
