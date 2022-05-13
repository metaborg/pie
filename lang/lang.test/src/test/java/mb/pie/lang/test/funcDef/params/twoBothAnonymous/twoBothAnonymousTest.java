package mb.pie.lang.test.funcDef.params.twoBothAnonymous;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoBothAnonymousTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggertwoBothAnonymousComponent.class,
            new main_twoBothAnonymous.Input(2, null),
            false
        );
    }
}
