package mb.pie.lang.test.binary.add.addPathPathAbsoluteRelative;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addPathPathAbsoluteRelativeComponent extends PieComponent {
    main_addPathPathAbsoluteRelative get();
}
