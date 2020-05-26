package mb.pie.lang.test.returnTypes.pathRelative;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface pathRelativeComponent extends PieComponent {
    main_pathRelative get();
}
