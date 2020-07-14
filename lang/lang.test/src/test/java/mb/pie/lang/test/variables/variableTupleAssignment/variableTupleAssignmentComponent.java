package mb.pie.lang.test.variables.variableTupleAssignment;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableTupleAssignmentComponent extends PieComponent {
    main_variableTupleAssignment get();
}
