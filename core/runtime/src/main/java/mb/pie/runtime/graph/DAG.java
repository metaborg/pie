package mb.pie.runtime.graph;

public class DAG<V> extends DirectedAcyclicGraph<V, DefaultEdge> {
    public DAG() {
        super(DefaultEdge.class);
    }
}
