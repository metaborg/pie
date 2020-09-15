package mb.pie.lang.test.controlFlow.listComprehension.intsLitToInts;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class IntsLitToIntsTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerintsLitToIntsComponent.class, new ArrayList<>(Arrays.asList(3, 4, 5)));
    }
}
