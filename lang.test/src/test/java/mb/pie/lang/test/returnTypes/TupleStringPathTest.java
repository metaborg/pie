package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class TupleStringPathTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_tupleStringPath(), main_tupleStringPath.class, new Tuple2("Folder with pictures", new FSPath("/c/home/bob/pictures")));
    }
}
