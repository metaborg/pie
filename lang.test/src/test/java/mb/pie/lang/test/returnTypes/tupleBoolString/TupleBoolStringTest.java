package mb.pie.lang.test.returnTypes.tupleBoolString;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TupleBoolStringTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertupleBoolStringComponent.class, new main_tupleBoolString.Output(false, "hey"));
    }
}
