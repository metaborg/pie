package mb.pie.lang.test.call.foreignTask.twoParam;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.foreignTask.twoParam.main_twoParam;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface twoParamComponent extends PieComponent {
    main_twoParam get();
}
