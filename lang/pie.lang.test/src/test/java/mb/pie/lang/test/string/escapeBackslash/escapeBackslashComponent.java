package mb.pie.lang.test.string.escapeBackslash;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.string.escapeBackslash.main_escapeBackslash;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface escapeBackslashComponent extends PieComponent {
    main_escapeBackslash get();
}
