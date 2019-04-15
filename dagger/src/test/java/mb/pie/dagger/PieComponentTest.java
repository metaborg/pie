package mb.pie.dagger;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.LambdaTaskDef;
import mb.pie.api.Logger;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.TaskDef;
import mb.pie.runtime.logger.StreamLogger;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PieComponentTest {
    // Create a module that binds task definitions separately and as a set.
    @Module static class TestTaskDefsModule {
        @Provides @TaskDefsScope LambdaTaskDef<None, String> providesCreateString() {
            return new LambdaTaskDef<>("createString", (ctx, input) -> "Hello, world!");
        }

        @Provides @TaskDefsScope LambdaTaskDef<String, String> providesModifyString() {
            return new LambdaTaskDef<>("modifyString", (ctx, input) -> input.substring(0, 7) + "universe!");
        }

        @Provides @TaskDefsScope @ElementsIntoSet Set<TaskDef<?, ?>> provideTaskDefs(
            LambdaTaskDef<None, String> createHelloWorldString,
            LambdaTaskDef<String, String> printString
        ) {
            final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
            taskDefs.add(createHelloWorldString);
            taskDefs.add(printString);
            return taskDefs;
        }
    }

    // Subclass TaskDefComponent to add concrete task definitions which can be used by the application, and to set
    // TestTaskDefsModule as a module of this component.
    @TaskDefsScope @Component(modules = TestTaskDefsModule.class)
    interface TestTaskDefsComponent extends TaskDefsComponent {
        LambdaTaskDef<None, String> createString();

        LambdaTaskDef<String, String> modifyString();
    }


    // Create a module that binds a verbose logger, overriding the optional binding of PieModule.
    @Module static class TestPieModule {
        @Provides @PieScope static Logger providesLogger() {
            return StreamLogger.verbose();
        }
    }

    // Subclass PieComponent to add TestPieModule as a module of this component.
    @PieScope
    @Component(modules = {PieModule.class, TestPieModule.class}, dependencies = {TestTaskDefsComponent.class})
    interface TestPieComponent extends PieComponent {

    }


    @Test void test() throws Exception {
        final TestTaskDefsComponent taskDefsComponent = DaggerPieComponentTest_TestTaskDefsComponent.create();
        assertSame(taskDefsComponent.createString(), taskDefsComponent.createString());
        assertSame(taskDefsComponent.modifyString(), taskDefsComponent.modifyString());
        assertTrue(taskDefsComponent.getTaskDefs().contains(taskDefsComponent.createString()));
        assertTrue(taskDefsComponent.getTaskDefs().contains(taskDefsComponent.modifyString()));

        final TestPieComponent pieComponent = DaggerPieComponentTest_TestPieComponent
            .builder()
            .testTaskDefsComponent(taskDefsComponent)
            .build();
        assertSame(pieComponent.getPie(), pieComponent.getPie());

        try(final Pie pie = pieComponent.getPie(); final PieSession session = pie.newSession()) {
            final String str1 = session.requireTopDown(taskDefsComponent.createString().createTask(None.instance));
            assertEquals("Hello, world!", str1);
            final String str2 = session.requireTopDown(taskDefsComponent.modifyString().createTask(str1));
            assertEquals("Hello, universe!", str2);
        }
    }
}