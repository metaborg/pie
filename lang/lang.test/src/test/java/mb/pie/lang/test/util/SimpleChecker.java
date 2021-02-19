package mb.pie.lang.test.util;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceComponent;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleChecker {
    public static <O extends Serializable> O assertTaskOutputEquals(
        Class<? extends PieComponent> componentClass, O expectedOutput) throws ExecException {
        return assertTaskOutputEquals(componentClass, None.instance, expectedOutput);
    }

    public static <I extends Serializable, O extends Serializable> O assertTaskOutputEquals(
        Class<? extends PieComponent> componentClass,
        I input,
        O expectedOutput
    ) throws ExecException {
        final O output = requireTask(componentClass, input);
        assertEquals(expectedOutput, output);
        return output;
    }


    public static <I extends Serializable, O extends Serializable> O requireTask(
        Class<? extends PieComponent> componentClass
    ) throws ExecException {
        return requireTask(componentClass, None.instance);
    }

    public static <I extends Serializable, O extends Serializable> O requireTask(
        Class<? extends PieComponent> componentClass,
        I input
    ) throws ExecException {
        try {
            Object builder = componentClass.getMethod("builder").invoke(null);
            builder.getClass().getMethod("pieModule", PieModule.class).invoke(builder, new PieModule(PieBuilderImpl::new));
            final LoggerComponent loggerComponent = DaggerLoggerComponent.builder().loggerModule(LoggerModule.noop()).build();
            builder.getClass().getMethod("loggerComponent", LoggerComponent.class).invoke(builder, loggerComponent);
            final RootResourceServiceComponent resourceServiceComponent = DaggerRootResourceServiceComponent.builder().loggerComponent(loggerComponent).build();
            builder.getClass().getMethod("resourceServiceComponent", ResourceServiceComponent.class).invoke(builder, resourceServiceComponent);
            PieComponent component = (PieComponent)builder.getClass().getMethod("build").invoke(builder);
            try(MixedSession session = component.getPie().newSession()) {
                @SuppressWarnings("unchecked") final TaskDef<I, O> main = (TaskDef<I, O>)component.getClass().getMethod("get").invoke(component);
                return session.require(main.createTask(input));
            }
        } catch(NoSuchMethodException e) {
            throw new RuntimeException("Expected method to exist", e);
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Expected method to be accessible", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
