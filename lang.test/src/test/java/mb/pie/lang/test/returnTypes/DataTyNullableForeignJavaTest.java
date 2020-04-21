package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class DataTyNullableForeignJavaTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(new TaskDefsModule_dataTyNullableForeignJavaTestGen(), main_dataTyNullableForeignJava.class, null);
    }
}
