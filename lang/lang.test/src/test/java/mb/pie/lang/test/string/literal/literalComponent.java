package mb.pie.lang.test.string.literal;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface literalComponent extends PieComponent {
    main_literal get();
}
