package mb.pie.lang.test.path.list.listWithExtension;

import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import org.junit.jupiter.api.Test;

class listWithExtensionTest extends PathTestBase {
    @Test void test() throws ExecException {
        assertOutputEquals(DaggerlistWithExtensionComponent.class, fsPathsOf("A.txt", "C.txt", "D.txt"));
    }
}
