package mb.pie.lang.test.variables.anonymous.multiAllAnonymousDiscard;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class multiAllAnonymousDiscardTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggermultiAllAnonymousDiscardComponent.class, false);
    }
}
