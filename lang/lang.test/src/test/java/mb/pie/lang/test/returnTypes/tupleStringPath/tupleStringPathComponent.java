package mb.pie.lang.test.returnTypes.tupleStringPath;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface tupleStringPathComponent extends PieComponent {
    main_tupleStringPath get();
}
