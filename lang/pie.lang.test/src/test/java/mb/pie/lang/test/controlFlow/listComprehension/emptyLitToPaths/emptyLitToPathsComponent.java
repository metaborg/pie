package mb.pie.lang.test.controlFlow.listComprehension.emptyLitToPaths;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.controlFlow.listComprehension.emptyLitToPaths.main_emptyLitToPaths;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface emptyLitToPathsComponent extends PieComponent {
    main_emptyLitToPaths get();
}
