package mb.pie.dagger;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.LambdaTaskDef;
import mb.pie.api.Logger;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.logger.StreamLogger;
import org.junit.jupiter.api.Test;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PieComponentTest {
    /**
     * Module that provides two task definitions, and provides a verbose logger, overriding the optional binding of
     * PieModule.
     */
    @Module static abstract class TestPieModule {
        @Provides @Singleton static LambdaTaskDef<None, String> providesCreateString() {
            return new LambdaTaskDef<>("getCreateString", (ctx, input) -> "Hello, world!");
        }

        @Provides @Singleton static LambdaTaskDef<String, String> providesModifyString() {
            return new LambdaTaskDef<>("getModifyString", (ctx, input) -> input.substring(0, 7) + "universe!");
        }

        @Provides @Singleton @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefs(
            LambdaTaskDef<None, String> createHelloWorldString,
            LambdaTaskDef<String, String> printString
        ) {
            final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
            taskDefs.add(createHelloWorldString);
            taskDefs.add(printString);
            return taskDefs;
        }


        @Provides @Singleton static Logger providesLogger() {
            return StreamLogger.verbose();
        }
    }

    /**
     * Component that subclasses {@link PieComponent} and provides the task definitions.
     */
    @Singleton
    @Component(modules = {PieModule.class, TestPieModule.class})
    interface TestPieComponent extends PieComponent {
        LambdaTaskDef<None, String> getCreateString();

        LambdaTaskDef<String, String> getModifyString();

        Set<TaskDef<?, ?>> getTaskDefs();
    }


    @Test void test() throws Exception {
        final TestPieComponent pieComponent = DaggerPieComponentTest_TestPieComponent
            .builder()
            .pieModule(new PieModule(PieBuilderImpl::new))
            .build();
        assertSame(pieComponent.getCreateString(), pieComponent.getCreateString());
        assertSame(pieComponent.getModifyString(), pieComponent.getModifyString());
        assertTrue(pieComponent.getTaskDefs().contains(pieComponent.getCreateString()));
        assertTrue(pieComponent.getTaskDefs().contains(pieComponent.getModifyString()));
        assertSame(pieComponent.getPie(), pieComponent.getPie());

        try(final Pie pie = pieComponent.getPie(); final MixedSession session = pie.newSession()) {
            final String str1 = session.require(pieComponent.getCreateString().createTask(None.instance));
            assertEquals("Hello, world!", str1);
            final String str2 = session.require(pieComponent.getModifyString().createTask(str1));
            assertEquals("Hello, universe!", str2);
        }
    }
}
