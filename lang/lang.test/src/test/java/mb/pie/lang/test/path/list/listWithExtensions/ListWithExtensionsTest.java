package mb.pie.lang.test.path.list.listWithExtensions;

import mb.pie.api.ExecException;
import mb.pie.lang.test.path.PathTestBase;
import org.junit.jupiter.api.Test;

class listWithExtensionsTest extends PathTestBase {
    @Test void test() throws ExecException {
        assertOutputEquals(DaggerlistWithExtensionsComponent.class, fsPathsOf("B.tig", "E.jav"));
    }
}
