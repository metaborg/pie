package mb.pie.lang.test.call.foreignFunc.singleParam;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.foreignFunc.singleParam.main_singleParam;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface singleParamComponent extends PieComponent {
    main_singleParam get();
}
