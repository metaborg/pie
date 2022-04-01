package mb.pie.lang.test.path;

import mb.pie.api.ExecException;
import mb.pie.dagger.PieComponent;
import mb.resource.fs.FSPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static mb.pie.lang.test.util.SimpleChecker.requireTask;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathTestBase {
    public static final FSPath SAMPLE_DIR = new FSPath("src/test/resources/sampleDir");

    public static HashSet<FSPath> fsPathsOf(String... paths) {
        HashSet<FSPath> res = new HashSet<>();
        for (String path : paths) {
            res.add(SAMPLE_DIR.appendRelativePath(path));
        }
        return res;
    }

    public static void assertOutputEquals(Class<? extends PieComponent> componentClass, Set<FSPath> expected) throws ExecException {
        ArrayList<FSPath> output = requireTask(componentClass, SAMPLE_DIR);
        HashSet<FSPath> actual = new HashSet<>(output);
        assertEquals(expected, actual);
    }
}
