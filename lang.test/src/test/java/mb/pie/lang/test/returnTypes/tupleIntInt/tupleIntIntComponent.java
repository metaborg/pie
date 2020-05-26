package mb.pie.lang.test.returnTypes.tupleIntInt;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface tupleIntIntComponent extends PieComponent {
    main_tupleIntInt get();
}
