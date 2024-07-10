package mb.pie.lang.test.controlFlow.listComprehension.stringsValToStrings;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class StringsValToStringsTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerstringsValToStringsComponent.class, new ArrayList<>(Arrays.asList("Hello name", "World name")));
    }
}
