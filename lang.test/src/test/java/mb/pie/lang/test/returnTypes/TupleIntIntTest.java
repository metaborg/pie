package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TupleIntIntTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_tupleIntInt(), main_tupleIntInt.class, new main_tupleIntInt.Output(4, -90));
    }
}
