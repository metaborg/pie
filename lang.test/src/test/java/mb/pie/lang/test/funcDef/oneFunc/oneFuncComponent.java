package mb.pie.lang.test.funcDef.oneFunc;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface oneFuncComponent extends PieComponent {
    main_oneFunc get();
}
