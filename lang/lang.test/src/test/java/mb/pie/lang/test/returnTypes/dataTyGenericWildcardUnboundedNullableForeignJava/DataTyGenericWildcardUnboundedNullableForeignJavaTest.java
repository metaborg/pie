package mb.pie.lang.test.returnTypes.dataTyGenericWildcardUnboundedNullableForeignJava;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class DataTyGenericWildcardUnboundedNullableForeignJavaTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerdataTyGenericWildcardUnboundedNullableForeignJavaComponent.class, null);
    }
}
