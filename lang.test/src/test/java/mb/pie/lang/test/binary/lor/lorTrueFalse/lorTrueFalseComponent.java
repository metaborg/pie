package mb.pie.lang.test.binary.lor.lorTrueFalse;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface lorTrueFalseComponent extends PieComponent {
    main_lorTrueFalse get();
}
