package mb.pie.lang.test.returnTypes.dataTyGenericWildcardLowerBoundedNullableForeignJava;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class DataTyGenericWildcardLowerBoundedNullableForeignJavaTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerdataTyGenericWildcardLowerBoundedNullableForeignJavaComponent.class, null);
    }
}
