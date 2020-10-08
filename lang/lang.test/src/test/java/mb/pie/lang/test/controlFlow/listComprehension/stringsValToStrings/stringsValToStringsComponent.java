package mb.pie.lang.test.controlFlow.listComprehension.stringsValToStrings;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface stringsValToStringsComponent extends PieComponent {
    main_stringsValToStrings get();
}
