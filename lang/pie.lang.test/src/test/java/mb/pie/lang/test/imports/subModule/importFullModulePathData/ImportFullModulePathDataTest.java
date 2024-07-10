package mb.pie.lang.test.imports.subModule.importFullModulePathData;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullModulePathDataTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullModulePathDataComponent.class, None.instance);
    }
}
