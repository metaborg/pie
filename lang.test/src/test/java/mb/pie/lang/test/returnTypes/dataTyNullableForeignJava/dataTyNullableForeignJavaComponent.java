package mb.pie.lang.test.returnTypes.dataTyNullableForeignJava;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface dataTyNullableForeignJavaComponent extends PieComponent {
    main_dataTyNullableForeignJava get();
}
