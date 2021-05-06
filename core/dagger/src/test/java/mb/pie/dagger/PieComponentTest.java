package mb.pie.dagger;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.LambdaTaskDef;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceComponent;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PieComponentTest {
    /**
     * Module that provides two task definitions, and provides a verbose logger, overriding the optional binding of
     * PieModule.
     */
    @Module
    static abstract class TestPieModule {
        @Provides @PieScope static LambdaTaskDef<None, String> providesCreateString() {
            return new LambdaTaskDef<>("getCreateString", (ctx, input) -> "Hello, world!");
        }

        @Provides @PieScope static LambdaTaskDef<String, String> providesModifyString() {
            return new LambdaTaskDef<>("getModifyString", (ctx, input) -> input.substring(0, 7) + "universe!");
        }

        @Provides @PieScope @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefs(
            LambdaTaskDef<None, String> createHelloWorldString,
            LambdaTaskDef<String, String> printString
        ) {
            final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
            taskDefs.add(createHelloWorldString);
            taskDefs.add(printString);
            return taskDefs;
        }
    }

    /**
     * Component that subclasses {@link PieComponent} and provides the task definitions.
     */
    @PieScope
    @Component(
        modules = {
            PieModule.class,
            TestPieModule.class
        },
        dependencies = {
            LoggerComponent.class,
            ResourceServiceComponent.class
        }
    )
    interface TestPieComponent extends PieComponent {
        LambdaTaskDef<None, String> getCreateString();

        LambdaTaskDef<String, String> getModifyString();

        Set<TaskDef<?, ?>> getTaskDefs();
    }


    @Test void test() throws Exception {
        final LoggerComponent loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdOutVerbose())
            .build();
        final RootResourceServiceComponent resourceServiceComponent = DaggerRootResourceServiceComponent
            .builder()
            .loggerComponent(loggerComponent)
            .build();
        final TestPieComponent pieComponent = DaggerPieComponentTest_TestPieComponent.builder()
            .pieModule(new PieModule(PieBuilderImpl::new))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
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
