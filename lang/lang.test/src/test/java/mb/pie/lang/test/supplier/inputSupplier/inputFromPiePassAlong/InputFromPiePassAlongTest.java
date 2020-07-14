package mb.pie.lang.test.supplier.inputSupplier.inputFromPiePassAlong;

import mb.pie.api.Supplier;
import mb.pie.lang.test.supplier.inputSupplier.inputFromPieCreate.DaggerinputFromPieCreateComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class InputFromPiePassAlongTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerinputFromPiePassAlongComponent.class,
            (Supplier<String>) context -> "test-value",
            "value: 'test-value'");
        assertTaskOutputEquals(DaggerinputFromPiePassAlongComponent.class,
            (Supplier<String>) context -> "another string value",
            "value: 'another string value'");
    }
}
