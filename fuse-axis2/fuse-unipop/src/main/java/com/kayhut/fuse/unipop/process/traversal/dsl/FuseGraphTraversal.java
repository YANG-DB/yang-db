package com.kayhut.fuse.unipop.process.traversal.dsl;

import org.apache.tinkerpop.gremlin.process.computer.VertexProgram;
import org.apache.tinkerpop.gremlin.process.computer.traversal.step.map.PageRankVertexProgramStep;
import org.apache.tinkerpop.gremlin.process.computer.traversal.step.map.PeerPressureVertexProgramStep;
import org.apache.tinkerpop.gremlin.process.computer.traversal.step.map.ProgramVertexProgramStep;
import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.ByModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.Mutating;
import org.apache.tinkerpop.gremlin.process.traversal.step.TimesModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.TraverserSet;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalMetrics;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.util.function.ConstantSupplier;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 04/07/2017.
 */
public interface FuseGraphTraversal<S, E> extends GraphTraversal<S, E> {
     interface Admin<S, E> extends GraphTraversal.Admin<S, E>, FuseGraphTraversal<S, E> {

        @Override
        default <E2> FuseGraphTraversal.Admin<S, E2> addStep(final Step<?, E2> step) {
            return (FuseGraphTraversal.Admin<S, E2>) GraphTraversal.Admin.super.addStep((Step) step);
        }

        @Override
        default FuseGraphTraversal<S, E> iterate() {
            return FuseGraphTraversal.super.iterate();
        }

        @Override
         FuseGraphTraversal.Admin<S, E> clone();
    }

    @Override
    default FuseGraphTraversal.Admin<S, E> asAdmin() {
        return (FuseGraphTraversal.Admin<S, E>) this;
    }

    ///////////////////// MAP STEPS /////////////////////

    /**
     * Map a traverser referencing an object of type <code>E</code> to an object of type <code>E2</code>.
     *
     * @param function the lambda expression that does the functional mapping
     * @return the traversal with an appended {@link LambdaMapStep}.
     */
    default <E2> FuseGraphTraversal<S, E2> map(final Function<Traverser<E>, E2> function) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.map(function);
    }

    default <E2> FuseGraphTraversal<S, E2> map(final Traversal<?, E2> mapTraversal) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.map(mapTraversal);
    }

    /**
     * Map a {@link Traverser} referencing an object of type <code>E</code> to an iterator of objects of type <code>E2</code>.
     * The resultant iterator is drained one-by-one before a new <code>E</code> object is pulled in for processing.
     *
     * @param function the lambda expression that does the functional mapping
     * @param <E2>     the type of the returned iterator objects
     * @return the traversal with an appended {@link LambdaFlatMapStep}.
     */
    default <E2> FuseGraphTraversal<S, E2> flatMap(final Function<Traverser<E>, Iterator<E2>> function) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.flatMap(function);
    }

    /**
     * Map a {@link Traverser} referencing an object of type <code>E</code> to an iterator of objects of type <code>E2</code>.
     * The internal traversal is drained one-by-one before a new <code>E</code> object is pulled in for processing.
     *
     * @param flatMapTraversal the traversal generating objects of type <code>E2</code>
     * @param <E2>             the end type of the internal traversal
     * @return the traversal with an appended {@link TraversalFlatMapStep}.
     */
    default <E2> FuseGraphTraversal<S, E2> flatMap(final Traversal<?, E2> flatMapTraversal) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.flatMap(flatMapTraversal);
    }

    /**
     * Map the {@link Element} to its {@link Element#id}.
     *
     * @return the traversal with an appended {@link IdStep}.
     */
    default FuseGraphTraversal<S, Object> id() {
        return (FuseGraphTraversal<S, Object>)GraphTraversal.super.id();
    }

    /**
     * Map the {@link Element} to its {@link Element#label}.
     *
     * @return the traversal with an appended {@link LabelStep}.
     */
    default FuseGraphTraversal<S, String> label() {
        return (FuseGraphTraversal<S, String>)GraphTraversal.super.label();
    }

    /**
     * Map the <code>E</code> object to itself. In other words, a "no op."
     *
     * @return the traversal with an appended {@link IdentityStep}.
     */
    default FuseGraphTraversal<S, E> identity() {
        return (FuseGraphTraversal<S, E>)GraphTraversal.super.identity();
    }

    /**
     * Map any object to a fixed <code>E</code> value.
     *
     * @return the traversal with an appended {@link ConstantStep}.
     */
    default <E2> FuseGraphTraversal<S, E2> constant(final E2 e) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.constant(e);
    }

    default FuseGraphTraversal<S, Vertex> V(final Object... vertexIdsOrElements) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.V(vertexIdsOrElements);
    }

    /**
     * Map the {@link Vertex} to its adjacent vertices given a direction and edge labels.
     *
     * @param direction  the direction to traverse from the current vertex
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> to(final Direction direction, final String... edgeLabels) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.to(direction, edgeLabels);
    }

    /**
     * Map the {@link Vertex} to its outgoing adjacent vertices given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> out(final String... edgeLabels) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.out(edgeLabels);
    }

    /**
     * Map the {@link Vertex} to its incoming adjacent vertices given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> in(final String... edgeLabels) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.in(edgeLabels);
    }

    /**
     * Map the {@link Vertex} to its adjacent vertices given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> both(final String... edgeLabels) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.both(edgeLabels);
    }

    /**
     * Map the {@link Vertex} to its incident edges given the direction and edge labels.
     *
     * @param direction  the direction to traverse from the current vertex
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Edge> toE(final Direction direction, final String... edgeLabels) {
        return (FuseGraphTraversal<S, Edge>)GraphTraversal.super.toE(direction, edgeLabels);
    }

    /**
     * Map the {@link Vertex} to its outgoing incident edges given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Edge> outE(final String... edgeLabels) {
        return (FuseGraphTraversal<S, Edge>)GraphTraversal.super.outE(edgeLabels);
    }

    /**
     * Map the {@link Vertex} to its incoming incident edges given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Edge> inE(final String... edgeLabels) {
        return (FuseGraphTraversal<S, Edge>)GraphTraversal.super.inE(edgeLabels);
    }

    /**
     * Map the {@link Vertex} to its incident edges given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    default FuseGraphTraversal<S, Edge> bothE(final String... edgeLabels) {
        return (FuseGraphTraversal<S, Edge>)GraphTraversal.super.bothE(edgeLabels);
    }

    /**
     * Map the {@link Edge} to its incident vertices given the direction.
     *
     * @param direction the direction to traverser from the current edge
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> toV(final Direction direction) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.toV(direction);
    }

    /**
     * Map the {@link Edge} to its incoming/head incident {@link Vertex}.
     *
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> inV() {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.inV();
    }

    /**
     * Map the {@link Edge} to its outgoing/tail incident {@link Vertex}.
     *
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> outV() {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.outV();
    }

    /**
     * Map the {@link Edge} to its incident vertices.
     *
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> bothV() {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.bothV();
    }

    /**
     * Map the {@link Edge} to the incident vertex that was not just traversed from in the path history.
     *
     * @return the traversal with an appended {@link EdgeOtherVertexStep}.
     */
    default FuseGraphTraversal<S, Vertex> otherV() {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.otherV();
    }

    /**
     * Order all the objects in the traversal up to this point and then emit them one-by-one in their ordered sequence.
     *
     * @return the traversal with an appended {@link OrderGlobalStep}.
     */
    default FuseGraphTraversal<S, E> order() {
        return (FuseGraphTraversal<S, E>)GraphTraversal.super.order();
    }

    /**
     * Order either the {@link Scope#local} object (e.g. a list, map, etc.) or the entire {@link Scope#global} traversal stream.
     *
     * @param scope whether the ordering is the current local object or the entire global stream.
     * @return the traversal with an appended {@link OrderGlobalStep} or {@link OrderLocalStep}.
     */
    default FuseGraphTraversal<S, E> order(final Scope scope) {
        return (FuseGraphTraversal<S, E>)GraphTraversal.super.order(scope);
    }

    /**
     * Map the {@link Element} to its associated properties given the provide property keys.
     * If no property keys are provided, then all properties are emitted.
     *
     * @param propertyKeys the properties to retrieve
     * @param <E2>         the value type of the returned properties
     * @return the traversal with an appended {@link PropertiesStep}.
     */
    default <E2> FuseGraphTraversal<S, ? extends Property<E2>> properties(final String... propertyKeys) {
        return (FuseGraphTraversal<S, ? extends Property<E2>>)GraphTraversal.super.<E2>properties(propertyKeys);
    }

    /**
     * Map the {@link Element} to the values of the associated properties given the provide property keys.
     * If no property keys are provided, then all property values are emitted.
     *
     * @param propertyKeys the properties to retrieve their value from
     * @param <E2>         the value type of the properties
     * @return the traversal with an appended {@link PropertiesStep}.
     */
    default <E2> FuseGraphTraversal<S, E2> values(final String... propertyKeys) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.values(propertyKeys);
    }

    /**
     * Map the {@link Element} to a {@link Map} of the properties key'd according to their {@link Property#key}.
     * If no property keys are provided, then all properties are retrieved.
     *
     * @param propertyKeys the properties to retrieve
     * @param <E2>         the value type of the returned properties
     * @return the traversal with an appended {@link PropertyMapStep}.
     */
    default <E2> FuseGraphTraversal<S, Map<String, E2>> propertyMap(final String... propertyKeys) {
        return (FuseGraphTraversal<S, Map<String, E2>>)GraphTraversal.super.<E2>propertyMap(propertyKeys);
    }

    /**
     * Map the {@link Element} to a {@link Map} of the property values key'd according to their {@link Property#key}.
     * If no property keys are provided, then all property values are retrieved.
     *
     * @param propertyKeys the properties to retrieve
     * @param <E2>         the value type of the returned properties
     * @return the traversal with an appended {@link PropertyMapStep}.
     */
    default <E2> FuseGraphTraversal<S, Map<String, E2>> valueMap(final String... propertyKeys) {
        return (FuseGraphTraversal<S, Map<String, E2>>)GraphTraversal.super.<E2>valueMap(propertyKeys);
    }

    /**
     * Map the {@link Element} to a {@link Map} of the property values key'd according to their {@link Property#key}.
     * If no property keys are provided, then all property values are retrieved.
     *
     * @param includeTokens whether to include {@link T} tokens in the emitted map.
     * @param propertyKeys  the properties to retrieve
     * @param <E2>          the value type of the returned properties
     * @return the traversal with an appended {@link PropertyMapStep}.
     */
    default <E2> FuseGraphTraversal<S, Map<String, E2>> valueMap(final boolean includeTokens, final String... propertyKeys) {
        return (FuseGraphTraversal<S, Map<String, E2>>)GraphTraversal.super.<E2>valueMap(includeTokens, propertyKeys);
    }

    default <E2> FuseGraphTraversal<S, Collection<E2>> select(final Column column) {
        return (FuseGraphTraversal<S, Collection<E2>>)GraphTraversal.super.<E2>select(column);
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link GraphTraversal#select(Column)}
     */
    @Deprecated
    default <E2> FuseGraphTraversal<S, E2> mapValues() {
        return this.select(Column.values).unfold();
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link GraphTraversal#select(Column)}
     */
    @Deprecated
    default <E2> FuseGraphTraversal<S, E2> mapKeys() {
        return this.select(Column.keys).unfold();
    }

    /**
     * Map the {@link Property} to its {@link Property#key}.
     *
     * @return the traversal with an appended {@link PropertyKeyStep}.
     */
    default FuseGraphTraversal<S, String> key() {
        return (FuseGraphTraversal<S, String>)GraphTraversal.super.key();
    }

    /**
     * Map the {@link Property} to its {@link Property#value}.
     *
     * @return the traversal with an appended {@link PropertyValueStep}.
     */
    default <E2> FuseGraphTraversal<S, E2> value() {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.value();
    }

    /**
     * Map the {@link Traverser} to its {@link Path} history via {@link Traverser#path}.
     *
     * @return the traversal with an appended {@link PathStep}.
     */
    default FuseGraphTraversal<S, Path> path() {
        return (FuseGraphTraversal<S, Path>)GraphTraversal.super.path();
    }

    /**
     * Map the {@link Traverser} to a {@link Map} of bindings as specified by the provided match traversals.
     *
     * @param matchTraversals the traversal that maintain variables which must hold for the life of the traverser
     * @param <E2>            the type of the obejcts bound in the variables
     * @return the traversal with an appended {@link MatchStep}.
     */
    default <E2> FuseGraphTraversal<S, Map<String, E2>> match(final Traversal<?, ?>... matchTraversals) {
        return (FuseGraphTraversal<S,  Map<String, E2>>)GraphTraversal.super.<E2>match(matchTraversals);
    }

    /**
     * Map the {@link Traverser} to its {@link Traverser#sack} value.
     *
     * @param <E2> the sack value type
     * @return the traversal with an appended {@link SackStep}.
     */
    default <E2> FuseGraphTraversal<S, E2> sack() {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.sack();
    }

    default FuseGraphTraversal<S, Integer> loops() {
        return (FuseGraphTraversal<S, Integer>)GraphTraversal.super.loops();
    }

    default <E2> FuseGraphTraversal<S, Map<String, E2>> project(final String projectKey, final String... otherProjectKeys) {
        return (FuseGraphTraversal<S, Map<String, E2>>)GraphTraversal.super.<E2>project(projectKey, otherProjectKeys);
    }

    /**
     * Map the {@link Traverser} to a {@link Map} projection of sideEffect values, map values, and/or path values.
     *
     * @param pop             if there are multiple objects referenced in the path, the {@link Pop} to use.
     * @param selectKey1      the first key to project
     * @param selectKey2      the second key to project
     * @param otherSelectKeys the third+ keys to project
     * @param <E2>            the type of the objects projected
     * @return the traversal with an appended {@link SelectStep}.
     */
    default <E2> FuseGraphTraversal<S, Map<String, E2>> select(final Pop pop, final String selectKey1, final String selectKey2, String... otherSelectKeys) {
        return (FuseGraphTraversal<S, Map<String, E2>>)GraphTraversal.super.<E2>select(pop, selectKey1, selectKey2, otherSelectKeys);
    }

    /**
     * Map the {@link Traverser} to a {@link Map} projection of sideEffect values, map values, and/or path values.
     *
     * @param selectKey1      the first key to project
     * @param selectKey2      the second key to project
     * @param otherSelectKeys the third+ keys to project
     * @param <E2>            the type of the objects projected
     * @return the traversal with an appended {@link SelectStep}.
     */
    default <E2> FuseGraphTraversal<S, Map<String, E2>> select(final String selectKey1, final String selectKey2, String... otherSelectKeys) {
        return (FuseGraphTraversal<S, Map<String, E2>>)GraphTraversal.super.<E2>select(selectKey1, selectKey2, otherSelectKeys);
    }

    default <E2> FuseGraphTraversal<S, E2> select(final Pop pop, final String selectKey) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.select(pop, selectKey);
    }

    default <E2> FuseGraphTraversal<S, E2> select(final String selectKey) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.select(selectKey);
    }

    default <E2> FuseGraphTraversal<S, E2> unfold() {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.unfold();
    }

    default FuseGraphTraversal<S, List<E>> fold() {
        return (FuseGraphTraversal<S, List<E>>)GraphTraversal.super.fold();
    }

    default <E2> FuseGraphTraversal<S, E2> fold(final E2 seed, final BiFunction<E2, E, E2> foldFunction) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.fold(seed, foldFunction);
    }

    /**
     * Map the traversal stream to its reduction as a sum of the {@link Traverser#bulk} values (i.e. count the number of traversers up to this point).
     *
     * @return the traversal with an appended {@link CountGlobalStep}.
     */
    default FuseGraphTraversal<S, Long> count() {
        return (FuseGraphTraversal<S, Long>)GraphTraversal.super.count();
    }

    default FuseGraphTraversal<S, Long> count(final Scope scope) {
        return (FuseGraphTraversal<S, Long>)GraphTraversal.super.count(scope);
    }

    /**
     * Map the traversal stream to its reduction as a sum of the {@link Traverser#get} values multiplied by their {@link Traverser#bulk} (i.e. sum the traverser values up to this point).
     *
     * @return the traversal with an appended {@link SumGlobalStep}.
     */
    default <E2 extends Number> FuseGraphTraversal<S, E2> sum() {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.sum();
    }

    default <E2 extends Number> FuseGraphTraversal<S, E2> sum(final Scope scope) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.sum(scope);
    }

    default <E2 extends Number> FuseGraphTraversal<S, E2> max() {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.max();
    }

    default <E2 extends Number> FuseGraphTraversal<S, E2> max(final Scope scope) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.max(scope);
    }

    default <E2 extends Number> FuseGraphTraversal<S, E2> min() {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.min();
    }

    default <E2 extends Number> FuseGraphTraversal<S, E2> min(final Scope scope) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.min(scope);
    }

    default <E2 extends Number> FuseGraphTraversal<S, E2> mean() {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.mean();
    }

    default <E2 extends Number> FuseGraphTraversal<S, E2> mean(final Scope scope) {
        return (FuseGraphTraversal<S, E2>)GraphTraversal.super.mean(scope);
    }

    default <K, V> FuseGraphTraversal<S, Map<K, V>> group() {
        return (FuseGraphTraversal<S, Map<K, V>>)GraphTraversal.super.<K, V>group();
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #group()}
     */
    @Deprecated
    default <K, V> FuseGraphTraversal<S, Map<K, V>> groupV3d0() {
        return (FuseGraphTraversal<S, Map<K, V>>)GraphTraversal.super.<K, V>groupV3d0();
    }

    default <K> FuseGraphTraversal<S, Map<K, Long>> groupCount() {
        return (FuseGraphTraversal<S, Map<K, Long>>)GraphTraversal.super.<K>groupCount();
    }

    default FuseGraphTraversal<S, Tree> tree() {
        return (FuseGraphTraversal<S, Tree>)GraphTraversal.super.tree();
    }

    default FuseGraphTraversal<S, Vertex> addV(final String vertexLabel) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.addV(vertexLabel);
    }

    default FuseGraphTraversal<S, Vertex> addV() {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.addV();
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addV()}
     */
    @Deprecated
    default FuseGraphTraversal<S, Vertex> addV(final Object... propertyKeyValues) {
        return (FuseGraphTraversal<S, Vertex>)GraphTraversal.super.addV(propertyKeyValues);
    }

    default FuseGraphTraversal<S, Edge> addE(final String edgeLabel) {
        return (FuseGraphTraversal<S, Edge>)GraphTraversal.super.addE(edgeLabel);
    }

    default FuseGraphTraversal<S, E> to(final String toStepLabel) {
        return (FuseGraphTraversal<S, E>)GraphTraversal.super.to(toStepLabel);
    }

    default FuseGraphTraversal<S, E> from(final String fromStepLabel) {
        return (FuseGraphTraversal<S, E>)GraphTraversal.super.from(fromStepLabel);
    }

    default FuseGraphTraversal<S, E> to(final Traversal<E, Vertex> toVertex) {
        return (FuseGraphTraversal<S, E>)GraphTraversal.super.to(toVertex);
    }

    default FuseGraphTraversal<S, E> from(final Traversal<E, Vertex> fromVertex) {
        return (FuseGraphTraversal<S, E>)GraphTraversal.super.from(fromVertex);
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addE(String)}
     */
    @Deprecated
    default FuseGraphTraversal<S, Edge> addE(final Direction direction, final String firstVertexKeyOrEdgeLabel, final String edgeLabelOrSecondVertexKey, final Object... propertyKeyValues) {
        return (FuseGraphTraversal<S, Edge>)GraphTraversal.super.addE(direction, firstVertexKeyOrEdgeLabel, edgeLabelOrSecondVertexKey, propertyKeyValues);
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addE(String)}
     */
    @Deprecated
    default FuseGraphTraversal<S, Edge> addOutE(final String firstVertexKeyOrEdgeLabel, final String edgeLabelOrSecondVertexKey, final Object... propertyKeyValues) {
        return (FuseGraphTraversal<S, Edge>)GraphTraversal.super.addOutE(firstVertexKeyOrEdgeLabel, edgeLabelOrSecondVertexKey, propertyKeyValues);
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addE(String)}
     */
    @Deprecated
    default FuseGraphTraversal<S, Edge> addInE(final String firstVertexKeyOrEdgeLabel, final String edgeLabelOrSecondVertexKey, final Object... propertyKeyValues) {
        return this.addE(Direction.IN, firstVertexKeyOrEdgeLabel, edgeLabelOrSecondVertexKey, propertyKeyValues);
    }

    ///////////////////// FILTER STEPS /////////////////////

    default FuseGraphTraversal<S, E> filter(final Predicate<Traverser<E>> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.filter, predicate);
        return this.asAdmin().addStep(new LambdaFilterStep<>(this.asAdmin(), predicate));
    }

    default FuseGraphTraversal<S, E> filter(final Traversal<?, ?> filterTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.filter, filterTraversal);
        return this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), (Traversal) filterTraversal));
    }

    default FuseGraphTraversal<S, E> or(final Traversal<?, ?>... orTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.or, orTraversals);
        return this.asAdmin().addStep(new OrStep(this.asAdmin(), orTraversals));
    }

    default FuseGraphTraversal<S, E> and(final Traversal<?, ?>... andTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.and, andTraversals);
        return this.asAdmin().addStep(new AndStep(this.asAdmin(), andTraversals));
    }

    default FuseGraphTraversal<S, E> inject(final E... injections) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.inject, injections);
        return this.asAdmin().addStep(new InjectStep<>(this.asAdmin(), injections));
    }

    /**
     * Remove all duplicates in the traversal stream up to this point.
     *
     * @param scope       whether the deduplication is on the stream (global) or the current object (local).
     * @param dedupLabels if labels are provided, then the scope labels determine de-duplication. No labels implies current object.
     * @return the traversal with an appended {@link DedupGlobalStep}.
     */
    default FuseGraphTraversal<S, E> dedup(final Scope scope, final String... dedupLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.dedup, scope, dedupLabels);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new DedupGlobalStep<>(this.asAdmin(), dedupLabels) : new DedupLocalStep(this.asAdmin()));
    }

    /**
     * Remove all duplicates in the traversal stream up to this point.
     *
     * @param dedupLabels if labels are provided, then the scoped object's labels determine de-duplication. No labels implies current object.
     * @return the traversal with an appended {@link DedupGlobalStep}.
     */
    default FuseGraphTraversal<S, E> dedup(final String... dedupLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.dedup, dedupLabels);
        return this.asAdmin().addStep(new DedupGlobalStep<>(this.asAdmin(), dedupLabels));
    }

    default FuseGraphTraversal<S, E> where(final String startKey, final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.where, startKey, predicate);
        return this.asAdmin().addStep(new WherePredicateStep<>(this.asAdmin(), Optional.ofNullable(startKey), predicate));
    }

    default FuseGraphTraversal<S, E> where(final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.where, predicate);
        return this.asAdmin().addStep(new WherePredicateStep<>(this.asAdmin(), Optional.empty(), predicate));
    }

    default FuseGraphTraversal<S, E> where(final Traversal<?, ?> whereTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.where, whereTraversal);
        return TraversalHelper.getVariableLocations(whereTraversal.asAdmin()).isEmpty() ?
                this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), (Traversal) whereTraversal)) :
                this.asAdmin().addStep(new WhereTraversalStep<>(this.asAdmin(), whereTraversal));
    }

    default FuseGraphTraversal<S, E> has(final String propertyKey, final P<?> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
    }

    default FuseGraphTraversal<S, E> has(final T accessor, final P<?> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, accessor, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(accessor.getAccessor(), predicate));
    }

    default FuseGraphTraversal<S, E> has(final String propertyKey, final Object value) {
        if (value instanceof P)
            return this.has(propertyKey, (P) value);
        else if (value instanceof Traversal)
            return this.has(propertyKey, (Traversal) value);
        else {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, value);
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, P.eq(value)));
        }
    }

    default FuseGraphTraversal<S, E> has(final T accessor, final Object value) {
        if (value instanceof P)
            return this.has(accessor, (P) value);
        else if (value instanceof Traversal)
            return this.has(accessor, (Traversal) value);
        else {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, accessor, value);
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(accessor.getAccessor(), P.eq(value)));
        }
    }

    default FuseGraphTraversal<S, E> has(final String label, final String propertyKey, final P<?> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, predicate);
        TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), P.eq(label)));
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
    }

    default FuseGraphTraversal<S, E> has(final String label, final String propertyKey, final Object value) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, value);
        TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), P.eq(label)));
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, value instanceof P ? (P) value : P.eq(value)));
    }

    default FuseGraphTraversal<S, E> has(final T accessor, final Traversal<?, ?> propertyTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, accessor, propertyTraversal);
        return this.asAdmin().addStep(
                new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                        new PropertiesStep(propertyTraversal.asAdmin(), PropertyType.VALUE, accessor.getAccessor()))));
    }

    default FuseGraphTraversal<S, E> has(final String propertyKey, final Traversal<?, ?> propertyTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, propertyTraversal);
        return this.asAdmin().addStep(
                new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                        new PropertiesStep(propertyTraversal.asAdmin(), PropertyType.VALUE, propertyKey))));
    }

    default FuseGraphTraversal<S, E> has(final String propertyKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey);
        return this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), __.values(propertyKey)));
    }

    default FuseGraphTraversal<S, E> hasNot(final String propertyKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasNot, propertyKey);
        return this.asAdmin().addStep(new NotStep<>(this.asAdmin(), __.values(propertyKey)));
    }

    default FuseGraphTraversal<S, E> hasLabel(final String label, final String... otherLabels) {
        final String[] labels = new String[otherLabels.length + 1];
        labels[0] = label;
        System.arraycopy(otherLabels, 0, labels, 1, otherLabels.length);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, labels);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), labels.length == 1 ? P.eq(labels[0]) : P.within(labels)));
    }

    default FuseGraphTraversal<S, E> hasLabel(final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), predicate));
    }

    default FuseGraphTraversal<S, E> hasId(final Object id, final Object... otherIds) {
        if (id instanceof P)
            return this.hasId((P) id);
        else {
            final List<Object> ids = new ArrayList<>();
            if (id instanceof Object[]) {
                for (final Object i : (Object[]) id) {
                    ids.add(i);
                }
            } else
                ids.add(id);
            for (final Object i : otherIds) {
                if (i.getClass().isArray()) {
                    for (final Object ii : (Object[]) i) {
                        ids.add(ii);
                    }
                } else
                    ids.add(i);
            }
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasId, ids.toArray());
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.id.getAccessor(), ids.size() == 1 ? P.eq(ids.get(0)) : P.within(ids)));
        }
    }

    default FuseGraphTraversal<S, E> hasId(final P<Object> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasId, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.id.getAccessor(), predicate));
    }

    default FuseGraphTraversal<S, E> hasKey(final String label, final String... otherLabels) {
        final String[] labels = new String[otherLabels.length + 1];
        labels[0] = label;
        System.arraycopy(otherLabels, 0, labels, 1, otherLabels.length);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasKey, labels);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.key.getAccessor(), labels.length == 1 ? P.eq(labels[0]) : P.within(labels)));
    }

    default FuseGraphTraversal<S, E> hasKey(final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasKey, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.key.getAccessor(), predicate));
    }

    default FuseGraphTraversal<S, E> hasValue(final Object value, final Object... otherValues) {
        if (value instanceof P)
            return this.hasValue((P) value);
        else {
            final List<Object> values = new ArrayList<>();
            if (value instanceof Object[]) {
                for (final Object v : (Object[]) value) {
                    values.add(v);
                }
            } else
                values.add(value);
            for (final Object v : otherValues) {
                if (v instanceof Object[]) {
                    for (final Object vv : (Object[]) v) {
                        values.add(vv);
                    }
                } else
                    values.add(v);
            }
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasValue, values.toArray());
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.value.getAccessor(), values.size() == 1 ? P.eq(values.get(0)) : P.within(values)));
        }
    }

    default FuseGraphTraversal<S, E> hasValue(final P<Object> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasValue, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.value.getAccessor(), predicate));
    }

    default FuseGraphTraversal<S, E> is(final P<E> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.is, predicate);
        return this.asAdmin().addStep(new IsStep<>(this.asAdmin(), predicate));
    }

    /**
     * Filter the <code>E</code> object if it is not {@link P#eq} to the provided value.
     *
     * @param value the value that the object must equal.
     * @return the traversal with an appended {@link IsStep}.
     */
    default FuseGraphTraversal<S, E> is(final Object value) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.is, value);
        return this.asAdmin().addStep(new IsStep<>(this.asAdmin(), value instanceof P ? (P<E>) value : P.eq((E) value)));
    }

    default FuseGraphTraversal<S, E> not(final Traversal<?, ?> notTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.not, notTraversal);
        return this.asAdmin().addStep(new NotStep<>(this.asAdmin(), (Traversal<E, ?>) notTraversal));
    }

    /**
     * Filter the <code>E</code> object given a biased coin toss.
     *
     * @param probability the probability that the object will pass through
     * @return the traversal with an appended {@link CoinStep}.
     */
    default FuseGraphTraversal<S, E> coin(final double probability) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.coin, probability);
        return this.asAdmin().addStep(new CoinStep<>(this.asAdmin(), probability));
    }

    default FuseGraphTraversal<S, E> range(final long low, final long high) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.range, low, high);
        return this.asAdmin().addStep(new RangeGlobalStep<>(this.asAdmin(), low, high));
    }

    default <E2> FuseGraphTraversal<S, E2> range(final Scope scope, final long low, final long high) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.range, scope, low, high);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new RangeGlobalStep<>(this.asAdmin(), low, high)
                : new RangeLocalStep<>(this.asAdmin(), low, high));
    }

    default FuseGraphTraversal<S, E> limit(final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.limit, limit);
        return this.asAdmin().addStep(new RangeGlobalStep<>(this.asAdmin(), 0, limit));
    }

    default <E2> FuseGraphTraversal<S, E2> limit(final Scope scope, final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.limit, scope, limit);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new RangeGlobalStep<>(this.asAdmin(), 0, limit)
                : new RangeLocalStep<>(this.asAdmin(), 0, limit));
    }

    default FuseGraphTraversal<S, E> tail() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tail);
        return this.asAdmin().addStep(new TailGlobalStep<>(this.asAdmin(), 1));
    }

    default FuseGraphTraversal<S, E> tail(final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tail, limit);
        return this.asAdmin().addStep(new TailGlobalStep<>(this.asAdmin(), limit));
    }

    default <E2> FuseGraphTraversal<S, E2> tail(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tail, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new TailGlobalStep<>(this.asAdmin(), 1)
                : new TailLocalStep<>(this.asAdmin(), 1));
    }

    default <E2> FuseGraphTraversal<S, E2> tail(final Scope scope, final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tail, scope, limit);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new TailGlobalStep<>(this.asAdmin(), limit)
                : new TailLocalStep<>(this.asAdmin(), limit));
    }

    /**
     * Once the first {@link Traverser} hits this step, a count down is started. Once the time limit is up, all remaining traversers are filtered out.
     *
     * @param timeLimit the count down time
     * @return the traversal with an appended {@link TimeLimitStep}
     */
    default FuseGraphTraversal<S, E> timeLimit(final long timeLimit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.timeLimit, timeLimit);
        return this.asAdmin().addStep(new TimeLimitStep<E>(this.asAdmin(), timeLimit));
    }

    /**
     * Filter the <code>E</code> object if its {@link Traverser#path} is not {@link Path#isSimple}.
     *
     * @return the traversal with an appended {@link SimplePathStep}.
     */
    default FuseGraphTraversal<S, E> simplePath() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.simplePath);
        return this.asAdmin().addStep(new SimplePathStep<>(this.asAdmin()));
    }

    /**
     * Filter the <code>E</code> object if its {@link Traverser#path} is {@link Path#isSimple}.
     *
     * @return the traversal with an appended {@link CyclicPathStep}.
     */
    default FuseGraphTraversal<S, E> cyclicPath() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.cyclicPath);
        return this.asAdmin().addStep(new CyclicPathStep<>(this.asAdmin()));
    }

    default FuseGraphTraversal<S, E> sample(final int amountToSample) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sample, amountToSample);
        return this.asAdmin().addStep(new SampleGlobalStep<>(this.asAdmin(), amountToSample));
    }

    default FuseGraphTraversal<S, E> sample(final Scope scope, final int amountToSample) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sample, scope, amountToSample);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new SampleGlobalStep<>(this.asAdmin(), amountToSample)
                : new SampleLocalStep<>(this.asAdmin(), amountToSample));
    }

    default FuseGraphTraversal<S, E> drop() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.drop);
        return this.asAdmin().addStep(new DropStep<>(this.asAdmin()));
    }

    ///////////////////// SIDE-EFFECT STEPS /////////////////////

    default FuseGraphTraversal<S, E> sideEffect(final Consumer<Traverser<E>> consumer) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sideEffect, consumer);
        return this.asAdmin().addStep(new LambdaSideEffectStep<>(this.asAdmin(), consumer));
    }

    default FuseGraphTraversal<S, E> sideEffect(final Traversal<?, ?> sideEffectTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sideEffect, sideEffectTraversal);
        return this.asAdmin().addStep(new TraversalSideEffectStep<>(this.asAdmin(), (Traversal) sideEffectTraversal));
    }

    default <E2> FuseGraphTraversal<S, E2> cap(final String sideEffectKey, final String... sideEffectKeys) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.cap, sideEffectKey, sideEffectKeys);
        return this.asAdmin().addStep(new SideEffectCapStep<>(this.asAdmin(), sideEffectKey, sideEffectKeys));
    }

    default FuseGraphTraversal<S, Edge> subgraph(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.subgraph, sideEffectKey);
        return this.asAdmin().addStep(new SubgraphStep(this.asAdmin(), sideEffectKey));
    }

    default FuseGraphTraversal<S, E> aggregate(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.aggregate, sideEffectKey);
        return this.asAdmin().addStep(new AggregateStep<>(this.asAdmin(), sideEffectKey));
    }

    default FuseGraphTraversal<S, E> group(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.group, sideEffectKey);
        return this.asAdmin().addStep(new GroupSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #group(String)}.
     */
    default FuseGraphTraversal<S, E> groupV3d0(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.groupV3d0, sideEffectKey);
        return this.asAdmin().addStep(new GroupSideEffectStepV3d0<>(this.asAdmin(), sideEffectKey));
    }

    default FuseGraphTraversal<S, E> groupCount(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.groupCount, sideEffectKey);
        return this.asAdmin().addStep(new GroupCountSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    default FuseGraphTraversal<S, E> tree(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tree, sideEffectKey);
        return this.asAdmin().addStep(new TreeSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    default <V, U> FuseGraphTraversal<S, E> sack(final BiFunction<V, U, V> sackOperator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sack, sackOperator);
        return this.asAdmin().addStep(new SackValueStep<>(this.asAdmin(), sackOperator));
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #sack(BiFunction)} with {@link #by(String)}.
     */
    @Deprecated
    default <V, U> FuseGraphTraversal<S, E> sack(final BiFunction<V, U, V> sackOperator, final String elementPropertyKey) {
        return this.sack(sackOperator).by(elementPropertyKey);
    }

    default FuseGraphTraversal<S, E> store(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.store, sideEffectKey);
        return this.asAdmin().addStep(new StoreStep<>(this.asAdmin(), sideEffectKey));
    }

    default FuseGraphTraversal<S, E> profile(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(Traversal.Symbols.profile, sideEffectKey);
        return this.asAdmin().addStep(new ProfileSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    @Override
    default FuseGraphTraversal<S, TraversalMetrics> profile() {
        return (FuseGraphTraversal<S, TraversalMetrics>) GraphTraversal.super.profile();
    }

    /**
     * Sets a {@link Property} value and related meta properties if supplied, if supported by the {@link Graph}
     * and if the {@link Element} is a {@link VertexProperty}.  This method is the long-hand version of
     * {@link #property(Object, Object, Object...)} with the difference that the
     * {@link org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality} can be supplied.
     * <p/>
     * Generally speaking, this method will append an {@link AddPropertyStep} to the {@link Traversal} but when
     * possible, this method will attempt to fold key/value pairs into an {@link AddVertexStep}, {@link AddEdgeStep} or
     * {@link AddVertexStartStep}.  This potential optimization can only happen if cardinality is not supplied
     * and when meta-properties are not included.
     *
     * @param cardinality the specified cardinality of the property where {@code null} will allow the {@link Graph}
     *                    to use itsdefault settings
     * @param key         the key for the property
     * @param value       the value for the property
     * @param keyValues   any meta properties to be assigned to this property
     */
    default FuseGraphTraversal<S, E> property(final VertexProperty.Cardinality cardinality, final Object key, final Object value, final Object... keyValues) {
        if (null == cardinality)
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.property, key, value, keyValues);
        else
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.property, cardinality, key, value, keyValues);
        // if it can be detected that this call to property() is related to an addV/E() then we can attempt to fold
        // the properties into that step to gain an optimization for those graphs that support such capabilities.
        if ((this.asAdmin().getEndStep() instanceof AddVertexStep || this.asAdmin().getEndStep() instanceof AddEdgeStep
                || this.asAdmin().getEndStep() instanceof AddVertexStartStep) && keyValues.length == 0 && null == cardinality) {
            ((Mutating) this.asAdmin().getEndStep()).addPropertyMutations(key, value);
        } else {
            this.asAdmin().addStep(new AddPropertyStep(this.asAdmin(), cardinality, key, value));
            ((AddPropertyStep) this.asAdmin().getEndStep()).addPropertyMutations(keyValues);
        }
        return this;
    }

    /**
     * Sets the key and value of a {@link Property}. If the {@link Element} is a {@link VertexProperty} and the
     * {@link Graph} supports it, meta properties can be set.  Use of this method assumes that the
     * {@link org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality} isdefaulted to {@code null} which
     * means that the default cardinality for the {@link Graph} will be used.
     * <p/>
     * This method is effectively calls
     * {@link #property(org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality, Object, Object, Object...)}
     * as {@code property(null, key, value, keyValues}.
     *
     * @param key       the key for the property
     * @param value     the value for the property
     * @param keyValues any meta properties to be assigned to this property
     */
    default FuseGraphTraversal<S, E> property(final Object key, final Object value, final Object... keyValues) {
        return key instanceof VertexProperty.Cardinality ?
                this.property((VertexProperty.Cardinality) key, value, keyValues[0],
                        keyValues.length > 1 ?
                                Arrays.copyOfRange(keyValues, 1, keyValues.length) :
                                new Object[]{}) :
                this.property(null, key, value, keyValues);
    }

    ///////////////////// BRANCH STEPS /////////////////////

    default <M, E2> FuseGraphTraversal<S, E2> branch(final Traversal<?, M> branchTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.branch, branchTraversal);
        final BranchStep<E, E2, M> branchStep = new BranchStep<>(this.asAdmin());
        branchStep.setBranchTraversal((Traversal.Admin<E, M>) branchTraversal);
        return this.asAdmin().addStep(branchStep);
    }

    default <M, E2> FuseGraphTraversal<S, E2> branch(final Function<Traverser<E>, M> function) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.branch, function);
        final BranchStep<E, E2, M> branchStep = new BranchStep<>(this.asAdmin());
        branchStep.setBranchTraversal((Traversal.Admin<E, M>) __.map(function));
        return this.asAdmin().addStep(branchStep);
    }

    default <M, E2> FuseGraphTraversal<S, E2> choose(final Traversal<?, M> choiceTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choiceTraversal);
        return this.asAdmin().addStep(new ChooseStep<>(this.asAdmin(), (Traversal.Admin<E, M>) choiceTraversal));
    }

    default <E2> FuseGraphTraversal<S, E2> choose(final Traversal<?, ?> traversalPredicate,
                                                     final Traversal<?, E2> trueChoice, final Traversal<?, E2> falseChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, traversalPredicate, trueChoice, falseChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) traversalPredicate, (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) falseChoice));
    }

    default <E2> FuseGraphTraversal<S, E2> choose(final Traversal<?, ?> traversalPredicate,
                                                     final Traversal<?, E2> trueChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, traversalPredicate, trueChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) traversalPredicate, (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) __.identity()));
    }

    default <M, E2> FuseGraphTraversal<S, E2> choose(final Function<E, M> choiceFunction) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choiceFunction);
        return this.asAdmin().addStep(new ChooseStep<>(this.asAdmin(), (Traversal.Admin<E, M>) __.map(new FunctionTraverser<>(choiceFunction))));
    }

    default <E2> FuseGraphTraversal<S, E2> choose(final Predicate<E> choosePredicate,
                                                     final Traversal<?, E2> trueChoice, final Traversal<?, E2> falseChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choosePredicate, trueChoice, falseChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) __.filter(new PredicateTraverser<>(choosePredicate)), (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) falseChoice));
    }

    default <E2> FuseGraphTraversal<S, E2> choose(final Predicate<E> choosePredicate,
                                                     final Traversal<?, E2> trueChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choosePredicate, trueChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) __.filter(new PredicateTraverser<>(choosePredicate)), (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) __.identity()));
    }

    default <E2> FuseGraphTraversal<S, E2> optional(final Traversal<?, E2> optionalTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.optional, optionalTraversal);
        return this.asAdmin().addStep(new ChooseStep<>(this.asAdmin(), (Traversal.Admin<E, ?>) optionalTraversal, (Traversal.Admin<E, E2>) optionalTraversal.asAdmin().clone(), (Traversal.Admin<E, E2>) __.<E2>identity()));
    }

    default <E2> FuseGraphTraversal<S, E2> union(final Traversal<?, E2>... unionTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.union, unionTraversals);
        return this.asAdmin().addStep(new UnionStep(this.asAdmin(), Arrays.copyOf(unionTraversals, unionTraversals.length, Traversal.Admin[].class)));
    }

    default <E2> FuseGraphTraversal<S, E2> coalesce(final Traversal<?, E2>... coalesceTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.coalesce, coalesceTraversals);
        return this.asAdmin().addStep(new CoalesceStep(this.asAdmin(), Arrays.copyOf(coalesceTraversals, coalesceTraversals.length, Traversal.Admin[].class)));
    }

    default FuseGraphTraversal<S, E> repeat(final Traversal<?, E> repeatTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.repeat, repeatTraversal);
        return RepeatStep.addRepeatToTraversal(this, (Traversal.Admin<E, E>) repeatTraversal);
    }

    default FuseGraphTraversal<S, E> emit(final Traversal<?, ?> emitTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.emit, emitTraversal);
        return RepeatStep.addEmitToTraversal(this, (Traversal.Admin<E, ?>) emitTraversal);
    }

    default FuseGraphTraversal<S, E> emit(final Predicate<Traverser<E>> emitPredicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.emit, emitPredicate);
        return RepeatStep.addEmitToTraversal(this, (Traversal.Admin<E, ?>) __.filter(emitPredicate));
    }

    default FuseGraphTraversal<S, E> emit() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.emit);
        return RepeatStep.addEmitToTraversal(this, TrueTraversal.instance());
    }

    default FuseGraphTraversal<S, E> until(final Traversal<?, ?> untilTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.until, untilTraversal);
        return RepeatStep.addUntilToTraversal(this, (Traversal.Admin<E, ?>) untilTraversal);
    }

    default FuseGraphTraversal<S, E> until(final Predicate<Traverser<E>> untilPredicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.until, untilPredicate);
        return RepeatStep.addEmitToTraversal(this, (Traversal.Admin<E, ?>) __.filter(untilPredicate));
    }

    default FuseGraphTraversal<S, E> times(final int maxLoops) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.times, maxLoops);
        if (this.asAdmin().getEndStep() instanceof TimesModulating) {
            ((TimesModulating) this.asAdmin().getEndStep()).modulateTimes(maxLoops);
            return this;
        } else
            return RepeatStep.addUntilToTraversal(this, new LoopTraversal<>(maxLoops));
    }

    default <E2> FuseGraphTraversal<S, E2> local(final Traversal<?, E2> localTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.local, localTraversal);
        return this.asAdmin().addStep(new LocalStep<>(this.asAdmin(), localTraversal.asAdmin()));
    }

    /////////////////// VERTEX PROGRAM STEPS ////////////////

    default FuseGraphTraversal<S, E> pageRank() {
        return this.pageRank(0.85d);
    }

    default FuseGraphTraversal<S, E> pageRank(final double alpha) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.pageRank, alpha);
        return this.asAdmin().addStep((Step<E, E>) new PageRankVertexProgramStep(this.asAdmin(), alpha));
    }

    default FuseGraphTraversal<S, E> peerPressure() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.peerPressure);
        return this.asAdmin().addStep((Step<E, E>) new PeerPressureVertexProgramStep(this.asAdmin()));
    }

    default FuseGraphTraversal<S, E> program(final VertexProgram<?> vertexProgram) {
        return this.asAdmin().addStep((Step<E, E>) new ProgramVertexProgramStep(this.asAdmin(), vertexProgram));
    }

    ///////////////////// UTILITY STEPS /////////////////////

    default FuseGraphTraversal<S, E> as(final String stepLabel, final String... stepLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.as, stepLabel, stepLabels);
        if (this.asAdmin().getSteps().size() == 0) this.asAdmin().addStep(new StartStep<>(this.asAdmin()));
        final Step<?, E> endStep = this.asAdmin().getEndStep();
        endStep.addLabel(stepLabel);
        for (final String label : stepLabels) {
            endStep.addLabel(label);
        }
        return this;
    }

    default FuseGraphTraversal<S, E> barrier() {
        return this.barrier(Integer.MAX_VALUE);
    }

    default FuseGraphTraversal<S, E> barrier(final int maxBarrierSize) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.barrier, maxBarrierSize);
        return this.asAdmin().addStep(new NoOpBarrierStep<>(this.asAdmin(), maxBarrierSize));
    }

    default FuseGraphTraversal<S, E> barrier(final Consumer<TraverserSet<Object>> barrierConsumer) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.barrier, barrierConsumer);
        return this.asAdmin().addStep(new LambdaCollectingBarrierStep<>(this.asAdmin(), (Consumer) barrierConsumer, Integer.MAX_VALUE));
    }


    //// BY-MODULATORS

    default FuseGraphTraversal<S, E> by() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy();
        return this;
    }

    default FuseGraphTraversal<S, E> by(final Traversal<?, ?> traversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, traversal);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(traversal.asAdmin());
        return this;
    }

    default FuseGraphTraversal<S, E> by(final T token) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, token);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(token);
        return this;
    }

    default FuseGraphTraversal<S, E> by(final String key) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, key);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(key);
        return this;
    }

    default <V> FuseGraphTraversal<S, E> by(final Function<V, Object> function) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, function);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(function);
        return this;
    }

    //// COMPARATOR BY-MODULATORS

    default <V> FuseGraphTraversal<S, E> by(final Traversal<?, ?> traversal, final Comparator<V> comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, traversal, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(traversal.asAdmin(), comparator);
        return this;
    }

    default FuseGraphTraversal<S, E> by(final Comparator<E> comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(comparator);
        return this;
    }

    default FuseGraphTraversal<S, E> by(final Order order) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, order);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(order);
        return this;
    }

    default <V> FuseGraphTraversal<S, E> by(final String key, final Comparator<V> comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, key, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(key, comparator);
        return this;
    }

    /*default <V> GraphTraversal<S, E> by(final Column column, final Comparator<V> comparator) {
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(column, comparator);
        return this;
    }

    default <V> GraphTraversal<S, E> by(final T token, final Comparator<V> comparator) {
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(token, comparator);
        return this;
    }*/

    default <U> FuseGraphTraversal<S, E> by(final Function<U, Object> function, final Comparator comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, function, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(function, comparator);
        return this;
    }

    ////

    default <M, E2> FuseGraphTraversal<S, E> option(final M pickToken, final Traversal<E, E2> traversalOption) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.option, pickToken, traversalOption);
        ((TraversalOptionParent<M, E, E2>) this.asAdmin().getEndStep()).addGlobalChildOption(pickToken, traversalOption.asAdmin());
        return this;
    }

    default <E2> FuseGraphTraversal<S, E> option(final Traversal<E, E2> traversalOption) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.option, traversalOption);
        return this.option(TraversalOptionParent.Pick.any, traversalOption.asAdmin());
    }

    ////

    @Override
    default FuseGraphTraversal<S, E> iterate() {
        GraphTraversal.super.iterate();
        return this;
    }

    ////

     static final class Symbols {

        private Symbols() {
            // static fields only
        }

        public static final String join = "join";
    }
}
