package mb.pie.lang.test.unary.notTrue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface notTrueComponent extends PieComponent {
    main_notTrue get();
}
