package mb.pie.lang.test.call.foreignMethod.generic;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.Bar;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class GenericTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(
            DaggergenericComponent.class,
            new Bar<>(6, 4, "unused"),
            6
        );
    }
}
