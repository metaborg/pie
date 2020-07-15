package mb.pie.lang.test.supplier.fromTaskAndGet.tripleInt;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class tripleIntTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertripleIntComponent.class, 0, 0);
        assertTaskOutputEquals(DaggertripleIntComponent.class, 1, 3);
        assertTaskOutputEquals(DaggertripleIntComponent.class, 2, 6);
        assertTaskOutputEquals(DaggertripleIntComponent.class, 3, 9);
        assertTaskOutputEquals(DaggertripleIntComponent.class, 15, 45);
    }
}
