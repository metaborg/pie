package mb.pie.lang.test.controlFlow.listComprehension.emptyValToPathsTypeHint;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EmptyValToPathsTypeHintTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggeremptyValToPathsTypeHintComponent.class, new ArrayList<>());
    }
}
