package mb.pie.lang.test.funcDef.params.MultiUnnamed;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class MultiUnnamedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggerMultiUnnamedComponent.class,
            new main_MultiUnnamed.Input(new ArrayList<>(), ""),
            "hello"
        );
    }
}
