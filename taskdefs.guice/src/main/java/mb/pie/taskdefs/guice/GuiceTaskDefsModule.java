package mb.pie.taskdefs.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * A module that binds [GuiceTaskDefs] as a singleton, which can be passed to a [PieBuilder] with [withGuiceTaskDefs].
 */
public class GuiceTaskDefsModule implements Module {
    @Override public void configure(Binder binder) {
        binder.bind(GuiceTaskDefs.class).in(Singleton.class);
    }
}
