package mb.pie.lang.test.controlFlow.listComprehension.intsLitToInts;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface intsLitToIntsComponent extends PieComponent {
    main_intsLitToInts get();
}
