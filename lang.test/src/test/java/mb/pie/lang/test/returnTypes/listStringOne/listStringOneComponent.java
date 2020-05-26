package mb.pie.lang.test.returnTypes.listStringOne;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface listStringOneComponent extends PieComponent {
    main_listStringOne get();
}
