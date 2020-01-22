package mb.pie.lang.test.binary.add;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class addListListTestGenTest {
    @Test void test() throws ExecException {
        ArrayList<Integer> expected = new ArrayList<>(Arrays.asList(1, 2, 3));
        assertTaskOutputEquals(new TaskDefsModule_addListListTestGenTestGen(), main_addListListTestGen.class, expected);
    }
}
