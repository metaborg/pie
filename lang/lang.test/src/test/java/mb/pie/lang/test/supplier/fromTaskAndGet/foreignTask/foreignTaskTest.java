package mb.pie.lang.test.supplier.fromTaskAndGet.foreignTask;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class foreignTaskTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerforeignTaskComponent.class, true, "Not(true) = false");
        assertTaskOutputEquals(DaggerforeignTaskComponent.class, false, "Not(false) = true");
    }
}
