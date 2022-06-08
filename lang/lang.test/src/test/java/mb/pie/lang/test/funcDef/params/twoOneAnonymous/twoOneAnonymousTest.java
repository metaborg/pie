package mb.pie.lang.test.funcDef.params.twoOneAnonymous;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoOneAnonymousTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggertwoOneAnonymousComponent.class,
            new main_twoOneAnonymous.Input(34, null),
            67
        );
    }
}
