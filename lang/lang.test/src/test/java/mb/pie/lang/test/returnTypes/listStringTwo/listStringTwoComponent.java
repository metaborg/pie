package mb.pie.lang.test.returnTypes.listStringTwo;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface listStringTwoComponent extends PieComponent {
    main_listStringTwo get();
}
