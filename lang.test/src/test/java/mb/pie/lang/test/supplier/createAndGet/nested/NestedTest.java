package mb.pie.lang.test.supplier.createAndGet.nested;

import mb.pie.lang.test.supplier.createAndGet.typeString.DaggertypeStringComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NestedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernestedComponent.class, true);
    }
}
