package mb.pie.lang.test.returnTypes.string;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface stringComponent extends PieComponent {
    main_string get();
}
