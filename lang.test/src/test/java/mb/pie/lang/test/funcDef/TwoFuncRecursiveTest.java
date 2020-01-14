package mb.pie.lang.test.funcDef;

import com.google.inject.Guice;
import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TwoFuncRecursiveTest {
    @Test void test() throws ExecException {
        final main_twoFuncRecursive main = Guice.createInjector(new TaskDefsModule_twoFuncRecursive()).getProvider(main_twoFuncRecursive.class).get();;
        // Don't call a recursive function
    }
}
