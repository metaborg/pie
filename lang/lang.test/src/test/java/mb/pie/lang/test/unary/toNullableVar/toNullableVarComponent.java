package mb.pie.lang.test.unary.toNullableVar;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface toNullableVarComponent extends PieComponent {
    main_toNullableVar get();
}
