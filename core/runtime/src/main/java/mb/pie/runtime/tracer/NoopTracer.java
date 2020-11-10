package mb.pie.runtime.tracer;

public class NoopTracer extends EmptyTracer {
    public static final NoopTracer instance = new NoopTracer();

    private NoopTracer() {}
}
