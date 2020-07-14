package mb.pie.lang.test.binary.add.addStrUnit;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addStrUnitComponent extends PieComponent {
    main_addStrUnit get();
}
