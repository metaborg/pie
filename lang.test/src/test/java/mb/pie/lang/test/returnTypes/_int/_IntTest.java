package mb.pie.lang.test.returnTypes._int;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class _IntTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(Dagger_intComponent.class, new Integer(6));
    }
}
