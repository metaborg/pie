package mb.pie.lang.test.supplier.inputSupplier.inputFromPieCreate;

import mb.pie.api.Supplier;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class InputFromPieCreateTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerinputFromPieCreateComponent.class, "value: 'a string'");
    }
}
