package mb.pie.lang.test.supplier.fromTaskAndGet.fullyQualifiedSupplier;

import mb.pie.lang.test.supplier.fromTaskAndGet.fullyQualifiedSupplier.DaggerfullyQualifiedSupplierComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class fullyQualifiedSupplierTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerfullyQualifiedSupplierComponent.class, "world", "Hello world!");
        assertTaskOutputEquals(DaggerfullyQualifiedSupplierComponent.class, "Bob", "Hello Bob!");
        assertTaskOutputEquals(DaggerfullyQualifiedSupplierComponent.class, "Sasha", "Hello Sasha!");
        assertTaskOutputEquals(DaggerfullyQualifiedSupplierComponent.class, "", "Hello !");
        assertTaskOutputEquals(DaggerfullyQualifiedSupplierComponent.class,
            "\"; DROP TABLE `students`; --",
            "Hello \"; DROP TABLE `students`; --!");
    }
}
