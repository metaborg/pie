package mb.pie.lang.test.string.expressionInterpolationList;

import mb.pie.api.*;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionInterpolationListTest {
    @Test void test() throws ExecException, InterruptedException {
        final expressionInterpolationListComponent component = DaggerexpressionInterpolationListComponent.builder()
            .pieModule(new PieModule(PieBuilderImpl::new))
            .build();
        final Pie pie = component.getPie();
        try(MixedSession session = pie.newSession()) {
            main_expressionInterpolationList main = component.get();
            final Tuple2<ArrayList<Integer>, String> output = session.require(main.createTask(None.instance));
            final String expected = "list: " + output.component1();
            final String actual = output.component2();
            assertEquals(expected, actual);
        }
    }
}
