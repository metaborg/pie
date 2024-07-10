package mb.pie.lang.test.funcDef.params.singleUnnamed;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleUnnamedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleUnnamedComponent.class, "unused", 9);
    }
}
