package mb.pie.lang.test.path.walk.walkWithExtensions;

import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import org.junit.jupiter.api.Test;

class walkWithExtensionsTest extends PathTestBase {
    @Test void test() throws ExecException {
        assertOutputEquals(DaggerwalkWithExtensionsComponent.class, fsPathsOf("B.tig", "E.jav"));
    }
}
