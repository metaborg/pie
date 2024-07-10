package mb.pie.lang.test.funcDef.params.twoBothUnnamed;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoBothUnnamedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggertwoBothUnnamedComponent.class,
            new main_twoBothUnnamed.Input(null, "hi"),
            "red"
        );
    }
}
