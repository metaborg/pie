package mb.pie.lang.test.returnTypes.listStringTwo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListStringTwoTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerlistStringTwoComponent.class, new ArrayList<>(Arrays.asList("hello", "world")));
    }
}
