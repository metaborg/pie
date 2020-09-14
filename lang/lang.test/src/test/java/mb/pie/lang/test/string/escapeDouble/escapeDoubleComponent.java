package mb.pie.lang.test.string.escapeDouble;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface escapeDoubleComponent extends PieComponent {
    main_escapeDouble get();
}
