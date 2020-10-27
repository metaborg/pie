package mb.pie.bench.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Simple time measurement functionality.
 */
public class Timer {
    /** ThreadMXBean for measuring CPU time. **/
    private final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    /** If precise CPU time measurements are available. **/
    private final boolean canLogCPUTime = false; // HACK: force use System.nanoTime which is much closer to JMH's measurements.

    /** Last starting time since start was called. **/
    private long startTime = 0;


    public Timer() {
        this(false);
    }

    public Timer(boolean start) {
        if(canLogCPUTime)
            mxBean.setThreadCpuTimeEnabled(true);
        if(start)
            start();
    }


    /**
     * Starts the timer, noting the current time.
     */
    public void start() {
        startTime = time();
    }

    /**
     * @return The duration, in nanoseconds, between the call to {@link #start()} and this invocation. This method can
     *         be called multiple times after one {@link #start()} invocation.
     */
    public long stop() {
        return time() - startTime;
    }

    /**
     * Resets the timer, forgetting the time noted when {@link #start()} was called.
     */
    public void reset() {
        startTime = 0;
    }


    private long time() {
        if(canLogCPUTime)
            return mxBean.getCurrentThreadCpuTime();
        else
            return System.nanoTime();
    }
}
