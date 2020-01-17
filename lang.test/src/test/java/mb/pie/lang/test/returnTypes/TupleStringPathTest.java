package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TupleStringPathTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_tupleStringPathTestGen(), main_tupleStringPath.class, new main_tupleStringPath.Output("Folder with pictures", new FSPath("/c/home/bob/pictures")));
    }
}
