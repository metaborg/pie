package mb.pie.dagger;

import dagger.Component;
import mb.pie.api.Pie;

@PieScope @Component(modules = PieModule.class, dependencies = {TaskDefsComponent.class})
public interface PieComponent {
    Pie getPie();
}
