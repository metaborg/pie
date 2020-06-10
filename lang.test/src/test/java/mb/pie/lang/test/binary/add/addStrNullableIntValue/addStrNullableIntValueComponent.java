package mb.pie.lang.test.binary.add.addStrNullableIntValue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface addStrNullableIntValueComponent extends PieComponent {
    main_addStrNullableIntValue get();
}
