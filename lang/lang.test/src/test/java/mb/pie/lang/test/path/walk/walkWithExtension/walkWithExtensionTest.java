package mb.pie.lang.test.path.walk.walkWithExtension;

import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import org.junit.jupiter.api.Test;

class walkWithExtensionTest extends PathTestBase {
    @Test void test() throws ExecException {
        assertOutputEquals(DaggerwalkWithExtensionComponent.class, fsPathsOf("A.txt", "C.txt", "D.txt"));
    }
}
