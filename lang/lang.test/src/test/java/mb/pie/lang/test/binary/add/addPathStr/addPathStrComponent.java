package mb.pie.lang.test.binary.add.addPathStr;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addPathStrComponent extends PieComponent {
    main_addPathStr get();
}
