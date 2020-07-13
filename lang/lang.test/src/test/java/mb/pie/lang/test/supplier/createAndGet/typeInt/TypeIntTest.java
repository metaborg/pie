package mb.pie.lang.test.supplier.createAndGet.typeInt;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TypeIntTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertypeIntComponent.class, 87);
    }
}
