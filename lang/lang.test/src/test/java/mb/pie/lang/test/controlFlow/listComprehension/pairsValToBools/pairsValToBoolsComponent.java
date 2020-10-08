package mb.pie.lang.test.controlFlow.listComprehension.pairsValToBools;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface pairsValToBoolsComponent extends PieComponent {
    main_pairsValToBools get();
}
