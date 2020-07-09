package mb.pie.lang.test.supplier.inputSupplier.inputFromPieCreate;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface inputFromPieCreateComponent extends PieComponent {
    main_inputFromPieCreate get();
}
