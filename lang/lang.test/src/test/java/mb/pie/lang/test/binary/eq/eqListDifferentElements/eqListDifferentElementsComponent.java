package mb.pie.lang.test.binary.eq.eqListDifferentElements;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqListDifferentElementsComponent extends PieComponent {
    main_eqListDifferentElements get();
}
