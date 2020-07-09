package mb.pie.lang.test.supplier.inputSupplier.inputFromPiePassAlong;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface inputFromPiePassAlongComponent extends PieComponent {
    main_inputFromPiePassAlong get();
}
