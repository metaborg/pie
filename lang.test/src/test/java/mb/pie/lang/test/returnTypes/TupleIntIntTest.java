package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class TupleIntIntTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_tupleIntInt(), main_tupleIntInt.class, new Tuple2(4, -90));
    }
}
