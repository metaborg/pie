package mb.pie.lang.test.returnTypes.listIntEmpty;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface listIntEmptyComponent extends PieComponent {
    main_listIntEmpty get();
}
