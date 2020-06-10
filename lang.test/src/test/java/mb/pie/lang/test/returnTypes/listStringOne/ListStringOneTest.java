package mb.pie.lang.test.returnTypes.listStringOne;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListStringOneTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerlistStringOneComponent.class, new ArrayList<>(Arrays.asList("first")));
    }
}
