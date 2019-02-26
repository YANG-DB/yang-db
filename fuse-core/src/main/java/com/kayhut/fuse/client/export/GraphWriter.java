/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.kayhut.fuse.client.export;

import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public interface GraphWriter {
    /**
     * Write the entire graph to a stream.
     *
     * @param outputStream the stream to write to.
     * @param g the Assignment to write to stream.
     */
    void writeGraph(final OutputStream outputStream, final Assignment g) throws IOException;

    /**
     * Write a vertex to a stream with its associated edges.  Only write edges as defined by the requested direction.
     *
     * @param outputStream the stream to write to.
     * @param v            the vertex to write.
     * @param direction    the direction of edges to write or null if no edges are to be written.
     */
    void writeVertex(final OutputStream outputStream, final Entity v, final Rel.Direction direction) throws IOException;

    /**
     * Write a vertex to a stream without writing its edges.
     *
     * @param outputStream the stream to write to.
     * @param v            the vertex to write.
     */
    void writeVertex(final OutputStream outputStream, final Entity v) throws IOException;


    /**
     * Write a list of vertices from a {@link Assignment} to a stream with its associated edges.  Only write edges as
     * defined by the requested direction.
     *
     * @param outputStream the stream to write to.
     * @param vertexIterator a traversal that returns a list of vertices.
     * @param direction the direction of edges to write or null if no edges are to be written.
     */
    default void writeVertices(final OutputStream outputStream, final Iterator<Entity> vertexIterator, final Rel.Direction direction) throws IOException {
        while (vertexIterator.hasNext()) {
            writeVertex(outputStream, vertexIterator.next(), direction);
        }
    }

    /**
     * Write a vertex to a stream without writing its edges.
     *
     * @param outputStream the stream to write to.
     * @param vertexIterator a iterator that returns a list of vertices.
     */
    public default void writeVertices(final OutputStream outputStream, final Iterator<Entity> vertexIterator) throws IOException {
        while (vertexIterator.hasNext()) {
            writeVertex(outputStream, vertexIterator.next());
        }
    }

    /**
     * Write an edge to a stream.
     *
     * @param outputStream the stream to write to.
     * @param e the edge to write.
     */
    void writeEdge(final OutputStream outputStream, final Relationship e) throws IOException;

    /**
     * Write a vertex property to a stream.
     *
     * @param outputStream the stream to write to.
     * @param vp the vertex property to write.
     */
    void writeVertexProperty(final OutputStream outputStream, final Property vp) throws IOException;

    /**
     * Write a property to a stream.
     *
     * @param outputStream the stream to write to.
     * @param p the property to write.
     */
    void writeProperty(final OutputStream outputStream, final Property p) throws IOException;

    /**
     * Writes an arbitrary object to the stream.
     *
     * @param outputStream the stream to write to.
     * @param object the object to write which will use the standard serializer set.
     */
    void writeObject(final OutputStream outputStream, final Object object) throws IOException;

    /**
     * Largely a marker interface for builder classes that construct a {@link GraphWriter}.
     */
    interface WriterBuilder<T extends GraphWriter> {
        /**
         * Creates the {@link GraphWriter} implementation given options provided to the {@link WriterBuilder}
         * implementation.
         */
        T create();
    }
}
