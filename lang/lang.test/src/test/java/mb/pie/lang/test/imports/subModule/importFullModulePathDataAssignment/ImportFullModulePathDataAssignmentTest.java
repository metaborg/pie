package mb.pie.lang.test.imports.subModule.importFullModulePathDataAssignment;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ImportFullModulePathDataAssignmentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerimportFullModulePathDataAssignmentComponent.class, None.instance);
    }
}
