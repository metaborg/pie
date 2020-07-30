package mb.pie.lang.test.string.escapeSingle;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface escapeSingleComponent extends PieComponent {
    main_escapeSingle get();
}
