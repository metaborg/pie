package mb.pie.lang.test.util;

import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.MixedSession;
import mb.pie.api.TaskDefs;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.logger.StreamLogger;

public class PieRunner {
    private final Pie pie;

    public PieRunner(TaskDefs taskDefs) {
        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withTaskDefs(taskDefs);
        pieBuilder.withLogger(StreamLogger.verbose());
        this.pie = pieBuilder.build();
    }

    public MixedSession newSession() {
        return pie.newSession();
    }
}
