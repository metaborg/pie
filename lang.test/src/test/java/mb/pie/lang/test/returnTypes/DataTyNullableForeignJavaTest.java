package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class DataTyNullableForeignJavaTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_dataTyNullableForeignJava(), main_dataTyNullableForeignJava.class, null);
    }
}
