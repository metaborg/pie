package mb.pie.lang.test.call.contextParams.three;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.contextParams.three.main_three;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface threeComponent extends PieComponent {
    main_three get();
}
