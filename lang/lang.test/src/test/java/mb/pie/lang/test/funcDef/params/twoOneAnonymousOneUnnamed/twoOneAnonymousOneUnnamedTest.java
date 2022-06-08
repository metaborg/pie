package mb.pie.lang.test.funcDef.params.twoOneAnonymousOneUnnamed;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoOneAnonymousOneUnnamedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggertwoOneAnonymousOneUnnamedComponent.class,
            new main_twoOneAnonymousOneUnnamed.Input(null, "some string"),
            3
        );
    }
}
