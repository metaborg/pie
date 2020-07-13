package mb.pie.lang.test.returnTypes.boolTrue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface boolTrueComponent extends PieComponent {
    main_boolTrue get();
}
