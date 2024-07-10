package mb.pie.lang.test.returnTypes.dataTyNullableForeignJava;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class DataTyNullableForeignJavaTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerdataTyNullableForeignJavaComponent.class, null);
    }
}
