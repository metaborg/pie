package mb.pie.lang.test.returnTypes.listIntEmpty;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListIntEmptyTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerlistIntEmptyComponent.class, new ArrayList<>());
    }
}
