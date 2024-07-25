package mb.pie.taskdefs.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.pie.runtime.PieBuilderImpl;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GuiceTaskDefsTest {
    public static class ReturnResultString implements TaskDef<Supplier<String>, String> {
        public static String id = "ReturnResultString";

        @Override public String getId() {
            return id;
        }

        @Override
        public String exec(ExecContext context, Supplier<String> input) throws IOException, ExecException, InterruptedException {
            return context.require(input);
        }
    }

    public static class ReturnInjectedString implements TaskDef<None, String> {
        public static String id = "ReturnInjectedString";

        private final String string;

        @Inject public ReturnInjectedString(String string) {
            this.string = string;
        }

        @Override public String getId() {
            return id;
        }

        @Override public String exec(ExecContext context, None input) {
            return string;
        }
    }

    static class TestTaskDefsModule extends TaskDefsModule {
        @Override protected void bindTaskDefs() {
            bindTaskDef(ReturnResultString.class, ReturnResultString.id);
            bindTaskDef(ReturnInjectedString.class, ReturnInjectedString.id);
        }
    }

    static class StringModule extends AbstractModule {
        private final String string;

        StringModule(String string) {
            this.string = string;
        }

        @Override protected void configure() {
            bind(String.class).toInstance(string);
        }
    }

    @Test
    void test() throws Exception {
        final String string = "Hello, enterprise!";
        final Injector injector =
            Guice.createInjector(new GuiceTaskDefsModule(), new TestTaskDefsModule(), new StringModule(string));
        final TaskDefs taskDefs = injector.getInstance(TaskDefs.class);
        assertNotNull(taskDefs);

        final ReturnResultString returnResultString = injector.getInstance(ReturnResultString.class);
        assertNotNull(returnResultString);
        assertSame(returnResultString, taskDefs.getTaskDef(ReturnResultString.id));
        final ReturnInjectedString returnInjectedString = injector.getInstance(ReturnInjectedString.class);
        assertNotNull(returnInjectedString);
        assertSame(returnInjectedString, taskDefs.getTaskDef(ReturnInjectedString.id));

        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withTaskDefs(taskDefs);
        pieBuilder.withLoggerFactory(StreamLoggerFactory.stdOutVeryVerbose());
        try(final Pie pie = pieBuilder.build(); final MixedSession session = pie.newSession()) {
            final String returnedString = session.require(
                returnResultString.createTask(returnInjectedString.createSupplier(None.instance)));
            assertEquals(string, returnedString);
        }
    }
}
