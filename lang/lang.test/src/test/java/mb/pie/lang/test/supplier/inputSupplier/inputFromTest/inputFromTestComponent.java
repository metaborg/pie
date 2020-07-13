package mb.pie.lang.test.supplier.inputSupplier.inputFromTest;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface inputFromTestComponent extends PieComponent {
    main_inputFromTest get();
}
