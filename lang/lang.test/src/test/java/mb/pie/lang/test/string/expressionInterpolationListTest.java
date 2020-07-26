package mb.pie.lang.test.string;

import com.google.inject.Guice;
import com.google.inject.Injector;
import mb.pie.api.*;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class expressionInterpolationListTest {
    @Test void test() throws ExecException {
        final Injector injector = Guice.createInjector(new GuiceTaskDefsModule(), new TaskDefsModule_expressionInterpolationListTestGen());
        final TaskDef<None, main_expressionInterpolationList.Output> main = injector.getInstance(main_expressionInterpolationList.class);
        final TaskDefs taskDefs = injector.getInstance(TaskDefs.class);
        final PieRunner pieRunner = new PieRunner(taskDefs);
        try(MixedSession session = pieRunner.newSession()) {
            final Tuple2<ArrayList<Integer>, String> output = session.require(main.createTask(None.instance));
            final String expected = "list: " + output.component1();
            final String actual = output.component2();
            assertEquals(expected, actual);
        }

    }
}
