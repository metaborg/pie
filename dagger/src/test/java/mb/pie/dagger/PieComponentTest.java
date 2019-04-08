package mb.pie.dagger;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.LambdaTaskDef;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.api.exec.TopDownSession;
import org.junit.jupiter.api.Test;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PieComponentTest {
    @Module
    static class TestTaskDefsModule {
        @Provides @Singleton LambdaTaskDef<None, String> providesCreateHelloWorldString() {
            return new LambdaTaskDef<>("createHelloWorldString", (ctx, input) -> "Hello, world!");
        }

        @Provides @Singleton LambdaTaskDef<String, String> providesModifyString() {
            return new LambdaTaskDef<>("modifyString", (ctx, input) -> input.substring(0, 7) + "universe!");
        }

        @Provides @ElementsIntoSet Set<TaskDef<?, ?>> provideTaskDefs(
            LambdaTaskDef<None, String> createHelloWorldString,
            LambdaTaskDef<String, String> printString
        ) {
            final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
            taskDefs.add(createHelloWorldString);
            taskDefs.add(printString);
            return taskDefs;
        }
    }

    @Singleton @Component(modules = TestTaskDefsModule.class)
    interface TestTaskDefsComponent extends TaskDefsComponent {
        LambdaTaskDef<None, String> createHelloWorldString();

        LambdaTaskDef<String, String> modifyString();
    }

    @Test void test() throws Exception {
        final TestTaskDefsComponent taskDefsComponent = DaggerPieComponentTest_TestTaskDefsComponent.create();
        assertEquals(taskDefsComponent.createHelloWorldString(), taskDefsComponent.createHelloWorldString());
        assertEquals(taskDefsComponent.modifyString(), taskDefsComponent.modifyString());
        final PieComponent pieComponent = DaggerPieComponent.builder().taskDefsComponent(taskDefsComponent).build();
        try(final Pie pie = pieComponent.getPie()) {
            final TopDownSession session = pie.getTopDownExecutor().newSession();
            final String str1 =
                session.requireInitial(taskDefsComponent.createHelloWorldString().createTask(None.instance));
            assertEquals("Hello, world!", str1);
            final String str2 = session.requireInitial(taskDefsComponent.modifyString().createTask(str1));
            assertEquals("Hello, universe!", str2);
        }
    }
}