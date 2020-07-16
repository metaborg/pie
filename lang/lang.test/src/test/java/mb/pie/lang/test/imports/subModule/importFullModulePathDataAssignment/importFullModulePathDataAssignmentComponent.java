package mb.pie.lang.test.imports.subModule.importFullModulePathDataAssignment;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullModulePathDataAssignmentComponent extends PieComponent {
    main_importFullModulePathDataAssignment get();
}
