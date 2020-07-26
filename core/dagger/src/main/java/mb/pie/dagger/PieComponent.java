package mb.pie.dagger;

import dagger.Component;
import mb.pie.api.Pie;

import javax.inject.Singleton;

@Singleton @Component(modules = PieModule.class)
public interface PieComponent {
    Pie getPie();
}
