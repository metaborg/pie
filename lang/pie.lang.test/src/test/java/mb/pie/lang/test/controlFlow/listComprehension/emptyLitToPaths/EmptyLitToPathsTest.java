package mb.pie.lang.test.controlFlow.listComprehension.emptyLitToPaths;

import mb.pie.lang.test.controlFlow.listComprehension.emptyLitToPaths.DaggeremptyLitToPathsComponent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EmptyLitToPathsTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggeremptyLitToPathsComponent.class, new ArrayList<>());
    }
}
