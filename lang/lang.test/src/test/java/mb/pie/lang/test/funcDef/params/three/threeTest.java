package mb.pie.lang.test.funcDef.params.three;

import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class threeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggerthreeComponent.class,
            new main_three.Input(-6, true, "Et tu, Brute?"),
            None.instance
        );
    }
}
