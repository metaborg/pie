package mb.pie.runtime.exec;

/**
 * HACK: global object for collecting build statistics.
 */
public class Stats {
    public static int requires = 0;
    public static int executions = 0;
    public static int fileReqs = 0;
    public static int fileGens = 0;
    public static int callReqs = 0;

    public static void reset() {
        requires = 0;
        executions = 0;
        fileReqs = 0;
        fileGens = 0;
        callReqs = 0;
    }

    public static void addRequires() {
        ++requires;
    }

    public static void addExecution() {
        ++executions;
    }

    public static void addFileReq() {
        ++fileReqs;
    }

    public static void addFileGen() {
        ++fileGens;
    }

    public static void addCallReq() {
        ++callReqs;
    }
}
