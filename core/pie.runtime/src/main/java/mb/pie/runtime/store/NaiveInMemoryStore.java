package mb.pie.runtime.store;

/**
 * Naive implementation of the in-memory store. Does not override naive methods from {@link InMemoryStoreBase}.
 */
public class NaiveInMemoryStore extends InMemoryStoreBase {
    @Override public String toString() {
        return "NaiveInMemoryStore()";
    }
}
