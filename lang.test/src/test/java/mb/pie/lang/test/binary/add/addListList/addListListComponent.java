package mb.pie.lang.test.binary.add.addListList;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addListListComponent extends PieComponent {
    main_addListList get();
}
