package mb.pie.lang.test.funcDef.params.threeOneAnonymousOneUnnamed;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class threeOneAnonymousOneUnnamedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggerthreeOneAnonymousOneUnnamedComponent.class,
            new main_threeOneAnonymousOneUnnamed.Input(1, 2, 3),
            37
        );
    }
}
