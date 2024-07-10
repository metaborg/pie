package mb.pie.lang.test.returnTypes.dataTyGenericNullableForeignJava;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class DataTyGenericNullableForeignJavaTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerdataTyGenericNullableForeignJavaComponent.class, null);
    }
}
