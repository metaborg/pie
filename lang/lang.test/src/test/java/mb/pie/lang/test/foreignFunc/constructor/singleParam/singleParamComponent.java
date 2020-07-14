package mb.pie.lang.test.foreignFunc.constructor.singleParam;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface singleParamComponent extends PieComponent {
    main_singleParam get();
}
