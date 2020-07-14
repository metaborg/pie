package mb.pie.lang.test.returnTypes.pathAbsolute;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface pathAbsoluteComponent extends PieComponent {
    main_pathAbsolute get();
}
