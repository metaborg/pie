package mb.pie.lang.test.funcDef.params.single;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleComponent.class, "Bob", "hello, Bob");
    }
}
