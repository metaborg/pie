package mb.pie.lang.test.funcDef.params.multiAnonymous;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class multiAnonymousTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggermultiAnonymousComponent.class,
            new main_multiAnonymous.Input(2, null),
            false
        );
    }
}
