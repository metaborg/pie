package mb.pie.lang.test.returnTypes.listStringEmpty;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListStringEmptyTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerlistStringEmptyComponent.class, new ArrayList<>());
    }
}
