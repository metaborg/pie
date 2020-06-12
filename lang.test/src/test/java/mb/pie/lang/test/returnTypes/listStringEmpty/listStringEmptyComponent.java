package mb.pie.lang.test.returnTypes.listStringEmpty;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface listStringEmptyComponent extends PieComponent {
    main_listStringEmpty get();
}
