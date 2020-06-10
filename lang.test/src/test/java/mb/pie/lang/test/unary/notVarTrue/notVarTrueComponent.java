package mb.pie.lang.test.unary.notVarTrue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface notVarTrueComponent extends PieComponent {
    main_notVarTrue get();
}
