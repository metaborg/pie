package mb.pie.lang.test.returnTypes.listIntOne;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface listIntOneComponent extends PieComponent {
    main_listIntOne get();
}
