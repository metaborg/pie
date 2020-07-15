package mb.pie.lang.test.returnTypes.listIntTwo;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface listIntTwoComponent extends PieComponent {
    main_listIntTwo get();
}
