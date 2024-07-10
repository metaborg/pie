package mb.pie.lang.test.variables.anonymous.multiUseNamed;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class multiUseNamedTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggermultiUseNamedComponent.class, 2);
    }
}
