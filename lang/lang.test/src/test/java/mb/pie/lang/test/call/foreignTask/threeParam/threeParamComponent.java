package mb.pie.lang.test.call.foreignTask.threeParam;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.foreignTask.threeParam.main_threeParam;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface threeParamComponent extends PieComponent {
    main_threeParam get();
}
