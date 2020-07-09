package mb.pie.lang.test.unary.notFalse;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface notFalseComponent extends PieComponent {
    main_notFalse get();
}
