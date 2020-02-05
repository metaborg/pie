package mb.pie.lang.test.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import mb.pie.api.*;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.pie.taskdefs.guice.TaskDefsModule;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleChecker {
    public static <O extends Serializable> void assertTaskOutputEquals(TaskDefsModule taskDefsModule, Class<? extends TaskDef<None, O>> taskClass, O expectedOutput) throws ExecException {
        assertTaskOutputEquals(taskDefsModule, taskClass, None.instance, expectedOutput);
    }

    private static <I extends Serializable, O extends Serializable> void assertTaskOutputEquals(TaskDefsModule taskDefsModule, Class<? extends TaskDef<I, O>> taskClass, I input, O expectedOutput) throws ExecException {
        final Injector injector = Guice.createInjector(new GuiceTaskDefsModule(), taskDefsModule);
        final TaskDef<I, O> main = injector.getInstance(taskClass);
        final TaskDefs taskDefs = injector.getInstance(TaskDefs.class);
        final PieRunner pieRunner = new PieRunner(taskDefs);
        try(PieSession session = pieRunner.newSession()) {
            final O actualOutput = session.require(main.createTask(input));
            assertEquals(expectedOutput, actualOutput);
        }
    }
}
