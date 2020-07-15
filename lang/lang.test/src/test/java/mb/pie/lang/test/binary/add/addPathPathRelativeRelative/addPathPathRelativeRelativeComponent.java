package mb.pie.lang.test.binary.add.addPathPathRelativeRelative;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addPathPathRelativeRelativeComponent extends PieComponent {
    main_addPathPathRelativeRelative get();
}
