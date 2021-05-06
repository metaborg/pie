package mb.pie.bench.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Timer {
    public static class Time {
        public final long systemNanoTime;
        public final long threadCpuTime;
        public final long threadUserTime;

        public Time(long systemNanoTime, long threadCpuTime, long threadUserTime) {
            this.systemNanoTime = systemNanoTime;
            this.threadCpuTime = threadCpuTime;
            this.threadUserTime = threadUserTime;
        }
    }


    private final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    private long startSystemNanoTime = 0;
    private long startThreadCpuTime = 0;
    private long startThreadUserTime = 0;


    public Timer(boolean start) {
        mxBean.setThreadCpuTimeEnabled(true);
        if(start) {
            start();
        }
    }

    public Timer() {
        this(false);
    }


    /**
     * Starts the timer, noting the current time.
     */
    public void start() {
        startSystemNanoTime = systemNanoTime();
        startThreadCpuTime = threadCpuTime();
        startThreadUserTime = threadUserTime();
    }

    /**
     * @return The duration, in nanoseconds, between the call to {@link #start()} and this invocation. This method can
     * be called multiple times after one {@link #start()} invocation.
     */
    public Time stop() {
        return new Time(systemNanoTime() - startSystemNanoTime, threadCpuTime() - startThreadCpuTime, threadUserTime() - startThreadUserTime);
    }

    /**
     * Resets the timer, forgetting the time noted when {@link #start()} was called.
     */
    public void reset() {
        startSystemNanoTime = 0;
        startThreadCpuTime = 0;
        startThreadUserTime = 0;
    }


    private long systemNanoTime() { return System.nanoTime(); }

    private long threadCpuTime() { return mxBean.getCurrentThreadCpuTime(); }

    private long threadUserTime() { return mxBean.getCurrentThreadUserTime(); }
}
