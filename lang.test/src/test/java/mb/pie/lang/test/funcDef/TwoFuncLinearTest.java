package mb.pie.lang.test.funcDef;

import com.google.inject.Guice;
import com.google.inject.Injector;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.api.TaskDefs;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TwoFuncLinearTest {
    @Test void test() throws ExecException {
    	final Injector injector = Guice.createInjector(new GuiceTaskDefsModule(), new TaskDefsModule_twoFuncLinear());
        final main_twoFuncLinear main = injector.getInstance(main_twoFuncLinear.class);
        final TaskDefs taskDefs = injector.getInstance(TaskDefs.class);
        final PieRunner pieRunner = new PieRunner(taskDefs);
        try(PieSession session = pieRunner.newSession()) {
            final None output = session.require(main.createTask(None.instance));
            assertEquals(None.instance, output);
        }
    }
}
