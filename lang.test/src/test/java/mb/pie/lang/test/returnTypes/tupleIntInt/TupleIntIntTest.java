package mb.pie.lang.test.returnTypes.tupleIntInt;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TupleIntIntTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertupleIntIntComponent.class, new main_tupleIntInt.Output(4, -90));
    }
}
