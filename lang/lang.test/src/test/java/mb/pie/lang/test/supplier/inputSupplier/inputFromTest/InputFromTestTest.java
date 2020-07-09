package mb.pie.lang.test.supplier.inputSupplier.inputFromTest;

import mb.pie.api.Supplier;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class InputFromTestTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerinputFromTestComponent.class, (Supplier<Integer>) context -> -20, -19);
    }
}
