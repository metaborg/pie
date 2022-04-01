package mb.pie.lang.test.returnTypes.dataTyGenericWildcardUpperBoundedNullableForeignJava;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class DataTyGenericWildcardUpperBoundedNullableForeignJavaTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerdataTyGenericWildcardUpperBoundedNullableForeignJavaComponent.class, null);
    }
}
