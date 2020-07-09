package mb.pie.lang.test.util;

import mb.pie.api.*;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleChecker {
    public static <O extends Serializable> O assertTaskOutputEquals(
        Class<? extends PieComponent> componentClass, O expectedOutput) throws ExecException {
        return assertTaskOutputEquals(componentClass, None.instance, expectedOutput);
    }

    public static <I extends Serializable, O extends Serializable> O assertTaskOutputEquals(
        Class<? extends PieComponent> componentClass, I input, O expectedOutput) throws ExecException {
        try {
            Object builder = componentClass.getMethod("builder").invoke(null);
            builder.getClass().getMethod("pieModule", PieModule.class).invoke(builder, new PieModule(PieBuilderImpl::new));
            PieComponent component = (PieComponent)builder.getClass().getMethod("build").invoke(builder);

            Pie pie = component.getPie();
            try(MixedSession session = pie.newSession()) {
                @SuppressWarnings("unchecked") final TaskDef<I, O> main =
                    (TaskDef<I, O>)component.getClass().getMethod("get").invoke(component);
                final O actualOutput = session.require(main.createTask(input));
                assertEquals(expectedOutput, actualOutput);
                return actualOutput;
            }
        } catch (NoSuchMethodException e) {
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
