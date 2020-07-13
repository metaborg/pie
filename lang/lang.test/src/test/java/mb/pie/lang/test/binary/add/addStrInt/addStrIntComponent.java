package mb.pie.lang.test.binary.add.addStrInt;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addStrIntComponent extends PieComponent {
    main_addStrInt get();
}
