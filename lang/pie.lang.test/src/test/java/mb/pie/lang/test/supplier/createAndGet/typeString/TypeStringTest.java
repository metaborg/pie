package mb.pie.lang.test.supplier.createAndGet.typeString;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TypeStringTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertypeStringComponent.class, "Hello world!");
    }
}
