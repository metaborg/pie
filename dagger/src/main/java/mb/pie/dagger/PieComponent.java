package mb.pie.dagger;

import dagger.Component;
import mb.pie.api.Pie;

//@Component(modules = PieModule.class)
public interface PieComponent {
    Pie getPie();
}
