package mb.pie.lang.test.call.foreignMethod.genericWildcard;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.Bar;
import mb.pie.lang.test.call.foreignMethod.genericWildcard.DaggergenericWildcardComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class GenericWildcardTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggergenericWildcardComponent.class, new Bar<>(7, 8, "nope"), 7);
    }
}
