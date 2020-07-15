package mb.pie.lang.test.binary.lor.lorFalseFalse;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface lorFalseFalseComponent extends PieComponent {
    main_lorFalseFalse get();
}
