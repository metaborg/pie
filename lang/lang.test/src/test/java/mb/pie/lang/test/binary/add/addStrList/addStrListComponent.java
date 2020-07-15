package mb.pie.lang.test.binary.add.addStrList;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addStrListComponent extends PieComponent {
    main_addStrList get();
}
