package mb.pie.lang.test.unary.notVarFalse;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface notVarFalseComponent extends PieComponent {
    main_notVarFalse get();
}
