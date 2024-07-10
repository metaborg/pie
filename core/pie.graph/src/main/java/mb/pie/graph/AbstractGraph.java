/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package mb.pie.graph;

import java.util.*;

/**
 * A skeletal implementation of the <code>Graph</code> interface, to minimize the effort required to
 * implement graph interfaces. This implementation is applicable to both: directed graphs and
 * undirected graphs.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @see Graph
 */
abstract class AbstractGraph<V, E>
    implements
    Graph<V, E>
{
    /**
     * Construct a new empty graph object.
     */
    protected AbstractGraph()
    {
    }

    /**
     * @see Graph#containsEdge(Object, Object)
     */
    @Override
    public boolean containsEdge(V sourceVertex, V targetVertex)
    {
        return getEdge(sourceVertex, targetVertex) != null;
    }

    /**
     * @see Graph#removeAllEdges(Collection)
     */
    @Override
    public boolean removeAllEdges(Collection<? extends E> edges)
    {
        boolean modified = false;

        for (E e : edges) {
            modified |= removeEdge(e);
        }

        return modified;
    }

    /**
     * @see Graph#removeAllEdges(Object, Object)
     */
    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex)
    {
        Set<E> removed = getAllEdges(sourceVertex, targetVertex);
        if (removed == null) {
            return null;
        }
        removeAllEdges(removed);

        return removed;
    }

    /**
     * @see Graph#removeAllVertices(Collection)
     */
    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices)
    {
        boolean modified = false;

        for (V v : vertices) {
            modified |= removeVertex(v);
        }

        return modified;
    }

    /**
     * Ensures that the specified vertex exists in this graph, or else throws exception.
     *
     * @param v vertex
     *
     * @return <code>true</code> if this assertion holds.
     *
     * @throws NullPointerException if specified vertex is <code>null</code>.
     * @throws IllegalArgumentException if specified vertex does not exist in this graph.
     */
    protected boolean assertVertexExist(V v)
    {
        if (containsVertex(v)) {
            return true;
        } else if (v == null) {
            throw new NullPointerException();
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + v.toString());
        }
    }

    /**
     * Removes all the edges in this graph that are also contained in the specified edge array.
     * After this call returns, this graph will contain no edges in common with the specified edges.
     * This method will invoke the {@link Graph#removeEdge(Object)} method.
     *
     * @param edges edges to be removed from this graph.
     *
     * @return <code>true</code> if this graph changed as a result of the call.
     *
     * @see Graph#removeEdge(Object)
     * @see Graph#containsEdge(Object)
     */
    protected boolean removeAllEdges(E[] edges)
    {
        boolean modified = false;

        for (E edge : edges) {
            modified |= removeEdge(edge);
        }

        return modified;
    }

    /**
     * Returns a hash code value for this graph. The hash code of a graph is defined to be the sum
     * of the hash codes of vertices and edges in the graph. It is also based on graph topology and
     * edges weights.
     *
     * @return the hash code value this graph
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        int hash = vertexSet().hashCode();

        final boolean isDirected = getType().isDirected();

        for (E e : edgeSet()) {
            int part = e.hashCode();

            int source = getEdgeSource(e).hashCode();
            int target = getEdgeTarget(e).hashCode();

            int pairing = source + target;
            if (isDirected) {
                // see http://en.wikipedia.org/wiki/Pairing_function (VK);
                pairing = ((pairing) * (pairing + 1) / 2) + target;
            }

            part = (31 * part) + pairing;
            part = (31 * part) + Double.hashCode(getEdgeWeight(e));

            hash += part;
        }

        return hash;
    }

    /**
     * Indicates whether some other object is "equal to" this graph. Returns <code>true</code> if
     * the given object is also a graph, the two graphs are instances of the same graph class, have
     * identical vertices and edges sets with the same weights.
     *
     * @param obj object to be compared for equality with this graph
     *
     * @return <code>true</code> if the specified object is equal to this graph
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Graph<V, E> g = TypeUtil.uncheckedCast(obj);

        if (!vertexSet().equals(g.vertexSet())) {
            return false;
        }
        if (edgeSet().size() != g.edgeSet().size()) {
            return false;
        }

        final boolean isDirected = getType().isDirected();
        for (E e : edgeSet()) {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            if (!g.containsEdge(e)) {
                return false;
            }

            V gSource = g.getEdgeSource(e);
            V gTarget = g.getEdgeTarget(e);

            if (isDirected) {
                if (!gSource.equals(source) || !gTarget.equals(target)) {
                    return false;
                }
            } else {
                if ((!gSource.equals(source) || !gTarget.equals(target))
                    && (!gSource.equals(target) || !gTarget.equals(source)))
                {
                    return false;
                }
            }

            if (Double.compare(getEdgeWeight(e), g.getEdgeWeight(e)) != 0) {
                return false;
            }
        }

        return true;
    }
}
