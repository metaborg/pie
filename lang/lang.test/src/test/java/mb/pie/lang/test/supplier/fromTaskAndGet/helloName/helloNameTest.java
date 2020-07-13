package mb.pie.lang.test.supplier.fromTaskAndGet.helloName;

import mb.pie.lang.test.supplier.fromTaskAndGet.helloName.DaggerhelloNameComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class helloNameTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerhelloNameComponent.class, "world", "Hello world!");
        assertTaskOutputEquals(DaggerhelloNameComponent.class, "Bob", "Hello Bob!");
        assertTaskOutputEquals(DaggerhelloNameComponent.class, "Sasha", "Hello Sasha!");
        assertTaskOutputEquals(DaggerhelloNameComponent.class, "", "Hello !");
        assertTaskOutputEquals(DaggerhelloNameComponent.class,
            "\"; DROP TABLE `students`; --",
            "Hello \"; DROP TABLE `students`; --!");
    }
}
