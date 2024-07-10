package mb.pie.lang.test.funcDef.params.twoOneUnnamed;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoOneUnnamedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggertwoOneUnnamedComponent.class,
            new main_twoOneUnnamed.Input(false, 127),
            false
        );
    }
}
