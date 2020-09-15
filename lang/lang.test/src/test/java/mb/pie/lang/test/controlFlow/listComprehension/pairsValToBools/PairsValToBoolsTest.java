package mb.pie.lang.test.controlFlow.listComprehension.pairsValToBools;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class PairsValToBoolsTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerpairsValToBoolsComponent.class, new ArrayList<>(Arrays.asList(true, false, false, true)));
    }
}
