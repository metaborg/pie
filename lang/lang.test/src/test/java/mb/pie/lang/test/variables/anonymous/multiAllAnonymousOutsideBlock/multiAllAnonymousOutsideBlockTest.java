package mb.pie.lang.test.variables.anonymous.multiAllAnonymousOutsideBlock;

import java.util.ArrayList;
import java.util.Arrays;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class multiAllAnonymousOutsideBlockTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggermultiAllAnonymousOutsideBlockComponent.class,
            new Tuple2<>(new ArrayList<>(Arrays.asList(3)), "nope")
        );
    }
}
