package mb.pie.lang.test.returnTypes.tupleStringPath;

import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TupleStringPathTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertupleStringPathComponent.class, new main_tupleStringPath.Output("Folder with pictures", new FSPath("/c/home/bob/pictures")));
    }
}
