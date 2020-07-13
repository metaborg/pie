package mb.pie.lang.test.foreignFunc.constructor.twoParam;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface twoParamComponent extends PieComponent {
    main_twoParam get();
}
