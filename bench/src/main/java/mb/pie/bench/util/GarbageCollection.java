package mb.pie.bench.util;

import java.lang.ref.WeakReference;

public class GarbageCollection {
    @SuppressWarnings({"ConstantConditions", "UnusedAssignment"})
    public static void run() {
        Object obj = new Object();
        final WeakReference ref = new WeakReference<>(obj);
        obj = null;
        do {
            System.gc();
        } while(ref.get() != null);
    }
}
