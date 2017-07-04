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
    public interface Admin<S, E> extends GraphTraversal.Admin<S, E>, FuseGraphTraversal<S, E> {

        @Override
        public default <E2> FuseGraphTraversal.Admin<S, E2> addStep(final Step<?, E2> step) {
            return (FuseGraphTraversal.Admin<S, E2>) GraphTraversal.Admin.super.addStep((Step) step);
        }

        @Override
        public default FuseGraphTraversal<S, E> iterate() {
            return FuseGraphTraversal.super.iterate();
        }

        @Override
        public FuseGraphTraversal.Admin<S, E> clone();
    }

    @Override
    public default FuseGraphTraversal.Admin<S, E> asAdmin() {
        return (FuseGraphTraversal.Admin<S, E>) this;
    }

    ///////////////////// MAP STEPS /////////////////////

    /**
     * Map a traverser referencing an object of type <code>E</code> to an object of type <code>E2</code>.
     *
     * @param function the lambda expression that does the functional mapping
     * @return the traversal with an appended {@link LambdaMapStep}.
     */
    public default <E2> FuseGraphTraversal<S, E2> map(final Function<Traverser<E>, E2> function) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.map, function);
        return this.asAdmin().addStep(new LambdaMapStep<>(this.asAdmin(), function));
    }

    public default <E2> FuseGraphTraversal<S, E2> map(final Traversal<?, E2> mapTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.map, mapTraversal);
        return this.asAdmin().addStep(new TraversalMapStep<>(this.asAdmin(), mapTraversal));
    }

    /**
     * Map a {@link Traverser} referencing an object of type <code>E</code> to an iterator of objects of type <code>E2</code>.
     * The resultant iterator is drained one-by-one before a new <code>E</code> object is pulled in for processing.
     *
     * @param function the lambda expression that does the functional mapping
     * @param <E2>     the type of the returned iterator objects
     * @return the traversal with an appended {@link LambdaFlatMapStep}.
     */
    public default <E2> FuseGraphTraversal<S, E2> flatMap(final Function<Traverser<E>, Iterator<E2>> function) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.flatMap, function);
        return this.asAdmin().addStep(new LambdaFlatMapStep<>(this.asAdmin(), function));
    }

    /**
     * Map a {@link Traverser} referencing an object of type <code>E</code> to an iterator of objects of type <code>E2</code>.
     * The internal traversal is drained one-by-one before a new <code>E</code> object is pulled in for processing.
     *
     * @param flatMapTraversal the traversal generating objects of type <code>E2</code>
     * @param <E2>             the end type of the internal traversal
     * @return the traversal with an appended {@link TraversalFlatMapStep}.
     */
    public default <E2> FuseGraphTraversal<S, E2> flatMap(final Traversal<?, E2> flatMapTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.flatMap, flatMapTraversal);
        return this.asAdmin().addStep(new TraversalFlatMapStep<>(this.asAdmin(), flatMapTraversal));
    }

    /**
     * Map the {@link Element} to its {@link Element#id}.
     *
     * @return the traversal with an appended {@link IdStep}.
     */
    public default FuseGraphTraversal<S, Object> id() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.id);
        return this.asAdmin().addStep(new IdStep<>(this.asAdmin()));
    }

    /**
     * Map the {@link Element} to its {@link Element#label}.
     *
     * @return the traversal with an appended {@link LabelStep}.
     */
    public default FuseGraphTraversal<S, String> label() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.label);
        return this.asAdmin().addStep(new LabelStep<>(this.asAdmin()));
    }

    /**
     * Map the <code>E</code> object to itself. In other words, a "no op."
     *
     * @return the traversal with an appended {@link IdentityStep}.
     */
    public default FuseGraphTraversal<S, E> identity() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.identity);
        return this.asAdmin().addStep(new IdentityStep<>(this.asAdmin()));
    }

    /**
     * Map any object to a fixed <code>E</code> value.
     *
     * @return the traversal with an appended {@link ConstantStep}.
     */
    public default <E2> FuseGraphTraversal<S, E2> constant(final E2 e) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.constant, e);
        return this.asAdmin().addStep(new ConstantStep<E, E2>(this.asAdmin(), e));
    }

    public default FuseGraphTraversal<S, Vertex> V(final Object... vertexIdsOrElements) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.V, vertexIdsOrElements);
        return this.asAdmin().addStep(new GraphStep<>(this.asAdmin(), Vertex.class, false, vertexIdsOrElements));
    }

    /**
     * Map the {@link Vertex} to its adjacent vertices given a direction and edge labels.
     *
     * @param direction  the direction to traverse from the current vertex
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> to(final Direction direction, final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.to, direction, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, direction, edgeLabels));
    }

    /**
     * Map the {@link Vertex} to its outgoing adjacent vertices given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> out(final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.out, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.OUT, edgeLabels));
    }

    /**
     * Map the {@link Vertex} to its incoming adjacent vertices given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> in(final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.in, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.IN, edgeLabels));
    }

    /**
     * Map the {@link Vertex} to its adjacent vertices given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> both(final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.both, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.BOTH, edgeLabels));
    }

    /**
     * Map the {@link Vertex} to its incident edges given the direction and edge labels.
     *
     * @param direction  the direction to traverse from the current vertex
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Edge> toE(final Direction direction, final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.toE, direction, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Edge.class, direction, edgeLabels));
    }

    /**
     * Map the {@link Vertex} to its outgoing incident edges given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Edge> outE(final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.outE, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Edge.class, Direction.OUT, edgeLabels));
    }

    /**
     * Map the {@link Vertex} to its incoming incident edges given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Edge> inE(final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.inE, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Edge.class, Direction.IN, edgeLabels));
    }

    /**
     * Map the {@link Vertex} to its incident edges given the edge labels.
     *
     * @param edgeLabels the edge labels to traverse
     * @return the traversal with an appended {@link VertexStep}.
     */
    public default FuseGraphTraversal<S, Edge> bothE(final String... edgeLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.bothE, edgeLabels);
        return this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Edge.class, Direction.BOTH, edgeLabels));
    }

    /**
     * Map the {@link Edge} to its incident vertices given the direction.
     *
     * @param direction the direction to traverser from the current edge
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> toV(final Direction direction) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.toV, direction);
        return this.asAdmin().addStep(new EdgeVertexStep(this.asAdmin(), direction));
    }

    /**
     * Map the {@link Edge} to its incoming/head incident {@link Vertex}.
     *
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> inV() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.inV);
        return this.asAdmin().addStep(new EdgeVertexStep(this.asAdmin(), Direction.IN));
    }

    /**
     * Map the {@link Edge} to its outgoing/tail incident {@link Vertex}.
     *
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> outV() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.outV);
        return this.asAdmin().addStep(new EdgeVertexStep(this.asAdmin(), Direction.OUT));
    }

    /**
     * Map the {@link Edge} to its incident vertices.
     *
     * @return the traversal with an appended {@link EdgeVertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> bothV() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.bothV);
        return this.asAdmin().addStep(new EdgeVertexStep(this.asAdmin(), Direction.BOTH));
    }

    /**
     * Map the {@link Edge} to the incident vertex that was not just traversed from in the path history.
     *
     * @return the traversal with an appended {@link EdgeOtherVertexStep}.
     */
    public default FuseGraphTraversal<S, Vertex> otherV() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.otherV);
        return this.asAdmin().addStep(new EdgeOtherVertexStep(this.asAdmin()));
    }

    /**
     * Order all the objects in the traversal up to this point and then emit them one-by-one in their ordered sequence.
     *
     * @return the traversal with an appended {@link OrderGlobalStep}.
     */
    public default FuseGraphTraversal<S, E> order() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.order);
        return this.asAdmin().addStep(new OrderGlobalStep<>(this.asAdmin()));
    }

    /**
     * Order either the {@link Scope#local} object (e.g. a list, map, etc.) or the entire {@link Scope#global} traversal stream.
     *
     * @param scope whether the ordering is the current local object or the entire global stream.
     * @return the traversal with an appended {@link OrderGlobalStep} or {@link OrderLocalStep}.
     */
    public default FuseGraphTraversal<S, E> order(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.order, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new OrderGlobalStep<>(this.asAdmin()) : new OrderLocalStep<>(this.asAdmin()));
    }

    /**
     * Map the {@link Element} to its associated properties given the provide property keys.
     * If no property keys are provided, then all properties are emitted.
     *
     * @param propertyKeys the properties to retrieve
     * @param <E2>         the value type of the returned properties
     * @return the traversal with an appended {@link PropertiesStep}.
     */
    public default <E2> FuseGraphTraversal<S, ? extends Property<E2>> properties(final String... propertyKeys) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.properties, propertyKeys);
        return this.asAdmin().addStep(new PropertiesStep<>(this.asAdmin(), PropertyType.PROPERTY, propertyKeys));
    }

    /**
     * Map the {@link Element} to the values of the associated properties given the provide property keys.
     * If no property keys are provided, then all property values are emitted.
     *
     * @param propertyKeys the properties to retrieve their value from
     * @param <E2>         the value type of the properties
     * @return the traversal with an appended {@link PropertiesStep}.
     */
    public default <E2> FuseGraphTraversal<S, E2> values(final String... propertyKeys) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, propertyKeys);
        return this.asAdmin().addStep(new PropertiesStep<>(this.asAdmin(), PropertyType.VALUE, propertyKeys));
    }

    /**
     * Map the {@link Element} to a {@link Map} of the properties key'd according to their {@link Property#key}.
     * If no property keys are provided, then all properties are retrieved.
     *
     * @param propertyKeys the properties to retrieve
     * @param <E2>         the value type of the returned properties
     * @return the traversal with an appended {@link PropertyMapStep}.
     */
    public default <E2> FuseGraphTraversal<S, Map<String, E2>> propertyMap(final String... propertyKeys) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.propertyMap, propertyKeys);
        return this.asAdmin().addStep(new PropertyMapStep<>(this.asAdmin(), false, PropertyType.PROPERTY, propertyKeys));
    }

    /**
     * Map the {@link Element} to a {@link Map} of the property values key'd according to their {@link Property#key}.
     * If no property keys are provided, then all property values are retrieved.
     *
     * @param propertyKeys the properties to retrieve
     * @param <E2>         the value type of the returned properties
     * @return the traversal with an appended {@link PropertyMapStep}.
     */
    public default <E2> FuseGraphTraversal<S, Map<String, E2>> valueMap(final String... propertyKeys) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.valueMap, propertyKeys);
        return this.asAdmin().addStep(new PropertyMapStep<>(this.asAdmin(), false, PropertyType.VALUE, propertyKeys));
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
    public default <E2> FuseGraphTraversal<S, Map<String, E2>> valueMap(final boolean includeTokens, final String... propertyKeys) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.valueMap, includeTokens, propertyKeys);
        return this.asAdmin().addStep(new PropertyMapStep<>(this.asAdmin(), includeTokens, PropertyType.VALUE, propertyKeys));
    }

    public default <E2> FuseGraphTraversal<S, Collection<E2>> select(final Column column) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.select, column);
        return this.asAdmin().addStep(new TraversalMapStep<>(this.asAdmin(), new ColumnTraversal(column)));
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link GraphTraversal#select(Column)}
     */
    @Deprecated
    public default <E2> FuseGraphTraversal<S, E2> mapValues() {
        return this.select(Column.values).unfold();
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link GraphTraversal#select(Column)}
     */
    @Deprecated
    public default <E2> FuseGraphTraversal<S, E2> mapKeys() {
        return this.select(Column.keys).unfold();
    }

    /**
     * Map the {@link Property} to its {@link Property#key}.
     *
     * @return the traversal with an appended {@link PropertyKeyStep}.
     */
    public default FuseGraphTraversal<S, String> key() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.key);
        return this.asAdmin().addStep(new PropertyKeyStep(this.asAdmin()));
    }

    /**
     * Map the {@link Property} to its {@link Property#value}.
     *
     * @return the traversal with an appended {@link PropertyValueStep}.
     */
    public default <E2> FuseGraphTraversal<S, E2> value() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.value);
        return this.asAdmin().addStep(new PropertyValueStep<>(this.asAdmin()));
    }

    /**
     * Map the {@link Traverser} to its {@link Path} history via {@link Traverser#path}.
     *
     * @return the traversal with an appended {@link PathStep}.
     */
    public default FuseGraphTraversal<S, Path> path() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.path);
        return this.asAdmin().addStep(new PathStep<>(this.asAdmin()));
    }

    /**
     * Map the {@link Traverser} to a {@link Map} of bindings as specified by the provided match traversals.
     *
     * @param matchTraversals the traversal that maintain variables which must hold for the life of the traverser
     * @param <E2>            the type of the obejcts bound in the variables
     * @return the traversal with an appended {@link MatchStep}.
     */
    public default <E2> FuseGraphTraversal<S, Map<String, E2>> match(final Traversal<?, ?>... matchTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.match, matchTraversals);
        return this.asAdmin().addStep(new MatchStep<>(this.asAdmin(), ConnectiveStep.Connective.AND, matchTraversals));
    }

    /**
     * Map the {@link Traverser} to its {@link Traverser#sack} value.
     *
     * @param <E2> the sack value type
     * @return the traversal with an appended {@link SackStep}.
     */
    public default <E2> FuseGraphTraversal<S, E2> sack() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sack);
        return this.asAdmin().addStep(new SackStep<>(this.asAdmin()));
    }

    public default FuseGraphTraversal<S, Integer> loops() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.loops);
        return this.asAdmin().addStep(new LoopsStep<>(this.asAdmin()));
    }

    public default <E2> FuseGraphTraversal<S, Map<String, E2>> project(final String projectKey, final String... otherProjectKeys) {
        final String[] projectKeys = new String[otherProjectKeys.length + 1];
        projectKeys[0] = projectKey;
        System.arraycopy(otherProjectKeys, 0, projectKeys, 1, otherProjectKeys.length);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.project, projectKey, otherProjectKeys);
        return this.asAdmin().addStep(new ProjectStep<>(this.asAdmin(), projectKeys));
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
    public default <E2> FuseGraphTraversal<S, Map<String, E2>> select(final Pop pop, final String selectKey1, final String selectKey2, String... otherSelectKeys) {
        final String[] selectKeys = new String[otherSelectKeys.length + 2];
        selectKeys[0] = selectKey1;
        selectKeys[1] = selectKey2;
        System.arraycopy(otherSelectKeys, 0, selectKeys, 2, otherSelectKeys.length);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.select, pop, selectKey1, selectKey2, otherSelectKeys);
        return this.asAdmin().addStep(new SelectStep<>(this.asAdmin(), pop, selectKeys));
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
    public default <E2> FuseGraphTraversal<S, Map<String, E2>> select(final String selectKey1, final String selectKey2, String... otherSelectKeys) {
        final String[] selectKeys = new String[otherSelectKeys.length + 2];
        selectKeys[0] = selectKey1;
        selectKeys[1] = selectKey2;
        System.arraycopy(otherSelectKeys, 0, selectKeys, 2, otherSelectKeys.length);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.select, selectKey1, selectKey2, otherSelectKeys);
        return this.asAdmin().addStep(new SelectStep<>(this.asAdmin(), null, selectKeys));
    }

    public default <E2> FuseGraphTraversal<S, E2> select(final Pop pop, final String selectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.select, pop, selectKey);
        return this.asAdmin().addStep(new SelectOneStep<>(this.asAdmin(), pop, selectKey));
    }

    public default <E2> FuseGraphTraversal<S, E2> select(final String selectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.select, selectKey);
        return this.asAdmin().addStep(new SelectOneStep<>(this.asAdmin(), null, selectKey));
    }

    public default <E2> FuseGraphTraversal<S, E2> unfold() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.unfold);
        return this.asAdmin().addStep(new UnfoldStep<>(this.asAdmin()));
    }

    public default FuseGraphTraversal<S, List<E>> fold() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.fold);
        return this.asAdmin().addStep(new FoldStep<>(this.asAdmin()));
    }

    public default <E2> FuseGraphTraversal<S, E2> fold(final E2 seed, final BiFunction<E2, E, E2> foldFunction) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.fold, seed, foldFunction);
        return this.asAdmin().addStep(new FoldStep<>(this.asAdmin(), new ConstantSupplier<>(seed), foldFunction)); // TODO: User should provide supplier?
    }

    /**
     * Map the traversal stream to its reduction as a sum of the {@link Traverser#bulk} values (i.e. count the number of traversers up to this point).
     *
     * @return the traversal with an appended {@link CountGlobalStep}.
     */
    public default FuseGraphTraversal<S, Long> count() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.count);
        return this.asAdmin().addStep(new CountGlobalStep<>(this.asAdmin()));
    }

    public default FuseGraphTraversal<S, Long> count(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.count, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new CountGlobalStep<>(this.asAdmin()) : new CountLocalStep<>(this.asAdmin()));
    }

    /**
     * Map the traversal stream to its reduction as a sum of the {@link Traverser#get} values multiplied by their {@link Traverser#bulk} (i.e. sum the traverser values up to this point).
     *
     * @return the traversal with an appended {@link SumGlobalStep}.
     */
    public default <E2 extends Number> FuseGraphTraversal<S, E2> sum() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sum);
        return this.asAdmin().addStep(new SumGlobalStep<>(this.asAdmin()));
    }

    public default <E2 extends Number> FuseGraphTraversal<S, E2> sum(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sum, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new SumGlobalStep<>(this.asAdmin()) : new SumLocalStep(this.asAdmin()));
    }

    public default <E2 extends Number> FuseGraphTraversal<S, E2> max() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.max);
        return this.asAdmin().addStep(new MaxGlobalStep<>(this.asAdmin()));
    }

    public default <E2 extends Number> FuseGraphTraversal<S, E2> max(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.max, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new MaxGlobalStep<>(this.asAdmin()) : new MaxLocalStep(this.asAdmin()));
    }

    public default <E2 extends Number> FuseGraphTraversal<S, E2> min() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.min);
        return this.asAdmin().addStep(new MinGlobalStep<>(this.asAdmin()));
    }

    public default <E2 extends Number> FuseGraphTraversal<S, E2> min(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.min, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new MinGlobalStep<E2>(this.asAdmin()) : new MinLocalStep<>(this.asAdmin()));
    }

    public default <E2 extends Number> FuseGraphTraversal<S, E2> mean() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.mean);
        return this.asAdmin().addStep(new MeanGlobalStep<>(this.asAdmin()));
    }

    public default <E2 extends Number> FuseGraphTraversal<S, E2> mean(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.mean, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new MeanGlobalStep<>(this.asAdmin()) : new MeanLocalStep(this.asAdmin()));
    }

    public default <K, V> FuseGraphTraversal<S, Map<K, V>> group() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.group);
        return this.asAdmin().addStep(new GroupStep<>(this.asAdmin()));
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #group()}
     */
    @Deprecated
    public default <K, V> FuseGraphTraversal<S, Map<K, V>> groupV3d0() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.groupV3d0);
        return this.asAdmin().addStep(new GroupStepV3d0<>(this.asAdmin()));
    }

    public default <K> FuseGraphTraversal<S, Map<K, Long>> groupCount() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.groupCount);
        return this.asAdmin().addStep(new GroupCountStep<>(this.asAdmin()));
    }

    public default FuseGraphTraversal<S, Tree> tree() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tree);
        return this.asAdmin().addStep(new TreeStep<>(this.asAdmin()));
    }

    public default FuseGraphTraversal<S, Vertex> addV(final String vertexLabel) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.addV, vertexLabel);
        return this.asAdmin().addStep(new AddVertexStep<>(this.asAdmin(), vertexLabel));
    }

    public default FuseGraphTraversal<S, Vertex> addV() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.addV);
        return this.asAdmin().addStep(new AddVertexStep<>(this.asAdmin(), null));
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addV()}
     */
    @Deprecated
    public default FuseGraphTraversal<S, Vertex> addV(final Object... propertyKeyValues) {
        this.addV();
        for (int i = 0; i < propertyKeyValues.length; i = i + 2) {
            this.property(propertyKeyValues[i], propertyKeyValues[i + 1]);
        }
        //((AddVertexStep) this.asAdmin().getEndStep()).addPropertyMutations(propertyKeyValues);
        return (FuseGraphTraversal<S, Vertex>) this;
    }

    public default FuseGraphTraversal<S, Edge> addE(final String edgeLabel) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.addE, edgeLabel);
        return this.asAdmin().addStep(new AddEdgeStep<>(this.asAdmin(), edgeLabel));
    }

    public default FuseGraphTraversal<S, E> to(final String toStepLabel) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.to, toStepLabel);
        ((AddEdgeStep) this.asAdmin().getEndStep()).addTo(__.select(toStepLabel));
        return this;
    }

    public default FuseGraphTraversal<S, E> from(final String fromStepLabel) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.from, fromStepLabel);
        ((AddEdgeStep) this.asAdmin().getEndStep()).addFrom(__.select(fromStepLabel));
        return this;
    }

    public default FuseGraphTraversal<S, E> to(final Traversal<E, Vertex> toVertex) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.to, toVertex);
        ((AddEdgeStep) this.asAdmin().getEndStep()).addTo(toVertex);
        return this;
    }

    public default FuseGraphTraversal<S, E> from(final Traversal<E, Vertex> fromVertex) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.from, fromVertex);
        ((AddEdgeStep) this.asAdmin().getEndStep()).addFrom(fromVertex);
        return this;
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addE(String)}
     */
    @Deprecated
    public default FuseGraphTraversal<S, Edge> addE(final Direction direction, final String firstVertexKeyOrEdgeLabel, final String edgeLabelOrSecondVertexKey, final Object... propertyKeyValues) {
        if (propertyKeyValues.length % 2 == 0) {
            // addOutE("createdBy", "a")
            this.addE(firstVertexKeyOrEdgeLabel);
            if (direction.equals(Direction.OUT))
                this.to(edgeLabelOrSecondVertexKey);
            else
                this.from(edgeLabelOrSecondVertexKey);

            for (int i = 0; i < propertyKeyValues.length; i = i + 2) {
                this.property(propertyKeyValues[i], propertyKeyValues[i + 1]);
            }
            //((Mutating) this.asAdmin().getEndStep()).addPropertyMutations(propertyKeyValues);
            return (FuseGraphTraversal<S, Edge>) this;
        } else {
            // addInE("a", "codeveloper", "b", "year", 2009)
            this.addE(edgeLabelOrSecondVertexKey);
            if (direction.equals(Direction.OUT))
                this.from(firstVertexKeyOrEdgeLabel).to((String) propertyKeyValues[0]);
            else
                this.to(firstVertexKeyOrEdgeLabel).from((String) propertyKeyValues[0]);

            for (int i = 1; i < propertyKeyValues.length; i = i + 2) {
                this.property(propertyKeyValues[i], propertyKeyValues[i + 1]);
            }
            //((Mutating) this.asAdmin().getEndStep()).addPropertyMutations(Arrays.copyOfRange(propertyKeyValues, 1, propertyKeyValues.length));
            return (FuseGraphTraversal<S, Edge>) this;
        }
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addE(String)}
     */
    @Deprecated
    public default FuseGraphTraversal<S, Edge> addOutE(final String firstVertexKeyOrEdgeLabel, final String edgeLabelOrSecondVertexKey, final Object... propertyKeyValues) {
        return this.addE(Direction.OUT, firstVertexKeyOrEdgeLabel, edgeLabelOrSecondVertexKey, propertyKeyValues);
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #addE(String)}
     */
    @Deprecated
    public default FuseGraphTraversal<S, Edge> addInE(final String firstVertexKeyOrEdgeLabel, final String edgeLabelOrSecondVertexKey, final Object... propertyKeyValues) {
        return this.addE(Direction.IN, firstVertexKeyOrEdgeLabel, edgeLabelOrSecondVertexKey, propertyKeyValues);
    }

    ///////////////////// FILTER STEPS /////////////////////

    public default FuseGraphTraversal<S, E> filter(final Predicate<Traverser<E>> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.filter, predicate);
        return this.asAdmin().addStep(new LambdaFilterStep<>(this.asAdmin(), predicate));
    }

    public default FuseGraphTraversal<S, E> filter(final Traversal<?, ?> filterTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.filter, filterTraversal);
        return this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), (Traversal) filterTraversal));
    }

    public default FuseGraphTraversal<S, E> or(final Traversal<?, ?>... orTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.or, orTraversals);
        return this.asAdmin().addStep(new OrStep(this.asAdmin(), orTraversals));
    }

    public default FuseGraphTraversal<S, E> and(final Traversal<?, ?>... andTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.and, andTraversals);
        return this.asAdmin().addStep(new AndStep(this.asAdmin(), andTraversals));
    }

    public default FuseGraphTraversal<S, E> inject(final E... injections) {
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
    public default FuseGraphTraversal<S, E> dedup(final Scope scope, final String... dedupLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.dedup, scope, dedupLabels);
        return this.asAdmin().addStep(scope.equals(Scope.global) ? new DedupGlobalStep<>(this.asAdmin(), dedupLabels) : new DedupLocalStep(this.asAdmin()));
    }

    /**
     * Remove all duplicates in the traversal stream up to this point.
     *
     * @param dedupLabels if labels are provided, then the scoped object's labels determine de-duplication. No labels implies current object.
     * @return the traversal with an appended {@link DedupGlobalStep}.
     */
    public default FuseGraphTraversal<S, E> dedup(final String... dedupLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.dedup, dedupLabels);
        return this.asAdmin().addStep(new DedupGlobalStep<>(this.asAdmin(), dedupLabels));
    }

    public default FuseGraphTraversal<S, E> where(final String startKey, final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.where, startKey, predicate);
        return this.asAdmin().addStep(new WherePredicateStep<>(this.asAdmin(), Optional.ofNullable(startKey), predicate));
    }

    public default FuseGraphTraversal<S, E> where(final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.where, predicate);
        return this.asAdmin().addStep(new WherePredicateStep<>(this.asAdmin(), Optional.empty(), predicate));
    }

    public default FuseGraphTraversal<S, E> where(final Traversal<?, ?> whereTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.where, whereTraversal);
        return TraversalHelper.getVariableLocations(whereTraversal.asAdmin()).isEmpty() ?
                this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), (Traversal) whereTraversal)) :
                this.asAdmin().addStep(new WhereTraversalStep<>(this.asAdmin(), whereTraversal));
    }

    public default FuseGraphTraversal<S, E> has(final String propertyKey, final P<?> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
    }

    public default FuseGraphTraversal<S, E> has(final T accessor, final P<?> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, accessor, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(accessor.getAccessor(), predicate));
    }

    public default FuseGraphTraversal<S, E> has(final String propertyKey, final Object value) {
        if (value instanceof P)
            return this.has(propertyKey, (P) value);
        else if (value instanceof Traversal)
            return this.has(propertyKey, (Traversal) value);
        else {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, value);
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, P.eq(value)));
        }
    }

    public default FuseGraphTraversal<S, E> has(final T accessor, final Object value) {
        if (value instanceof P)
            return this.has(accessor, (P) value);
        else if (value instanceof Traversal)
            return this.has(accessor, (Traversal) value);
        else {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, accessor, value);
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(accessor.getAccessor(), P.eq(value)));
        }
    }

    public default FuseGraphTraversal<S, E> has(final String label, final String propertyKey, final P<?> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, predicate);
        TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), P.eq(label)));
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
    }

    public default FuseGraphTraversal<S, E> has(final String label, final String propertyKey, final Object value) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, value);
        TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), P.eq(label)));
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, value instanceof P ? (P) value : P.eq(value)));
    }

    public default FuseGraphTraversal<S, E> has(final T accessor, final Traversal<?, ?> propertyTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, accessor, propertyTraversal);
        return this.asAdmin().addStep(
                new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                        new PropertiesStep(propertyTraversal.asAdmin(), PropertyType.VALUE, accessor.getAccessor()))));
    }

    public default FuseGraphTraversal<S, E> has(final String propertyKey, final Traversal<?, ?> propertyTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, propertyTraversal);
        return this.asAdmin().addStep(
                new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                        new PropertiesStep(propertyTraversal.asAdmin(), PropertyType.VALUE, propertyKey))));
    }

    public default FuseGraphTraversal<S, E> has(final String propertyKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey);
        return this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), __.values(propertyKey)));
    }

    public default FuseGraphTraversal<S, E> hasNot(final String propertyKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasNot, propertyKey);
        return this.asAdmin().addStep(new NotStep<>(this.asAdmin(), __.values(propertyKey)));
    }

    public default FuseGraphTraversal<S, E> hasLabel(final String label, final String... otherLabels) {
        final String[] labels = new String[otherLabels.length + 1];
        labels[0] = label;
        System.arraycopy(otherLabels, 0, labels, 1, otherLabels.length);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, labels);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), labels.length == 1 ? P.eq(labels[0]) : P.within(labels)));
    }

    public default FuseGraphTraversal<S, E> hasLabel(final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.label.getAccessor(), predicate));
    }

    public default FuseGraphTraversal<S, E> hasId(final Object id, final Object... otherIds) {
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

    public default FuseGraphTraversal<S, E> hasId(final P<Object> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasId, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.id.getAccessor(), predicate));
    }

    public default FuseGraphTraversal<S, E> hasKey(final String label, final String... otherLabels) {
        final String[] labels = new String[otherLabels.length + 1];
        labels[0] = label;
        System.arraycopy(otherLabels, 0, labels, 1, otherLabels.length);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasKey, labels);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.key.getAccessor(), labels.length == 1 ? P.eq(labels[0]) : P.within(labels)));
    }

    public default FuseGraphTraversal<S, E> hasKey(final P<String> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasKey, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.key.getAccessor(), predicate));
    }

    public default FuseGraphTraversal<S, E> hasValue(final Object value, final Object... otherValues) {
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

    public default FuseGraphTraversal<S, E> hasValue(final P<Object> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasValue, predicate);
        return TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(T.value.getAccessor(), predicate));
    }

    public default FuseGraphTraversal<S, E> is(final P<E> predicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.is, predicate);
        return this.asAdmin().addStep(new IsStep<>(this.asAdmin(), predicate));
    }

    /**
     * Filter the <code>E</code> object if it is not {@link P#eq} to the provided value.
     *
     * @param value the value that the object must equal.
     * @return the traversal with an appended {@link IsStep}.
     */
    public default FuseGraphTraversal<S, E> is(final Object value) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.is, value);
        return this.asAdmin().addStep(new IsStep<>(this.asAdmin(), value instanceof P ? (P<E>) value : P.eq((E) value)));
    }

    public default FuseGraphTraversal<S, E> not(final Traversal<?, ?> notTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.not, notTraversal);
        return this.asAdmin().addStep(new NotStep<>(this.asAdmin(), (Traversal<E, ?>) notTraversal));
    }

    /**
     * Filter the <code>E</code> object given a biased coin toss.
     *
     * @param probability the probability that the object will pass through
     * @return the traversal with an appended {@link CoinStep}.
     */
    public default FuseGraphTraversal<S, E> coin(final double probability) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.coin, probability);
        return this.asAdmin().addStep(new CoinStep<>(this.asAdmin(), probability));
    }

    public default FuseGraphTraversal<S, E> range(final long low, final long high) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.range, low, high);
        return this.asAdmin().addStep(new RangeGlobalStep<>(this.asAdmin(), low, high));
    }

    public default <E2> FuseGraphTraversal<S, E2> range(final Scope scope, final long low, final long high) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.range, scope, low, high);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new RangeGlobalStep<>(this.asAdmin(), low, high)
                : new RangeLocalStep<>(this.asAdmin(), low, high));
    }

    public default FuseGraphTraversal<S, E> limit(final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.limit, limit);
        return this.asAdmin().addStep(new RangeGlobalStep<>(this.asAdmin(), 0, limit));
    }

    public default <E2> FuseGraphTraversal<S, E2> limit(final Scope scope, final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.limit, scope, limit);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new RangeGlobalStep<>(this.asAdmin(), 0, limit)
                : new RangeLocalStep<>(this.asAdmin(), 0, limit));
    }

    public default FuseGraphTraversal<S, E> tail() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tail);
        return this.asAdmin().addStep(new TailGlobalStep<>(this.asAdmin(), 1));
    }

    public default FuseGraphTraversal<S, E> tail(final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tail, limit);
        return this.asAdmin().addStep(new TailGlobalStep<>(this.asAdmin(), limit));
    }

    public default <E2> FuseGraphTraversal<S, E2> tail(final Scope scope) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tail, scope);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new TailGlobalStep<>(this.asAdmin(), 1)
                : new TailLocalStep<>(this.asAdmin(), 1));
    }

    public default <E2> FuseGraphTraversal<S, E2> tail(final Scope scope, final long limit) {
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
    public default FuseGraphTraversal<S, E> timeLimit(final long timeLimit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.timeLimit, timeLimit);
        return this.asAdmin().addStep(new TimeLimitStep<E>(this.asAdmin(), timeLimit));
    }

    /**
     * Filter the <code>E</code> object if its {@link Traverser#path} is not {@link Path#isSimple}.
     *
     * @return the traversal with an appended {@link SimplePathStep}.
     */
    public default FuseGraphTraversal<S, E> simplePath() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.simplePath);
        return this.asAdmin().addStep(new SimplePathStep<>(this.asAdmin()));
    }

    /**
     * Filter the <code>E</code> object if its {@link Traverser#path} is {@link Path#isSimple}.
     *
     * @return the traversal with an appended {@link CyclicPathStep}.
     */
    public default FuseGraphTraversal<S, E> cyclicPath() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.cyclicPath);
        return this.asAdmin().addStep(new CyclicPathStep<>(this.asAdmin()));
    }

    public default FuseGraphTraversal<S, E> sample(final int amountToSample) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sample, amountToSample);
        return this.asAdmin().addStep(new SampleGlobalStep<>(this.asAdmin(), amountToSample));
    }

    public default FuseGraphTraversal<S, E> sample(final Scope scope, final int amountToSample) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sample, scope, amountToSample);
        return this.asAdmin().addStep(scope.equals(Scope.global)
                ? new SampleGlobalStep<>(this.asAdmin(), amountToSample)
                : new SampleLocalStep<>(this.asAdmin(), amountToSample));
    }

    public default FuseGraphTraversal<S, E> drop() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.drop);
        return this.asAdmin().addStep(new DropStep<>(this.asAdmin()));
    }

    ///////////////////// SIDE-EFFECT STEPS /////////////////////

    public default FuseGraphTraversal<S, E> sideEffect(final Consumer<Traverser<E>> consumer) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sideEffect, consumer);
        return this.asAdmin().addStep(new LambdaSideEffectStep<>(this.asAdmin(), consumer));
    }

    public default FuseGraphTraversal<S, E> sideEffect(final Traversal<?, ?> sideEffectTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sideEffect, sideEffectTraversal);
        return this.asAdmin().addStep(new TraversalSideEffectStep<>(this.asAdmin(), (Traversal) sideEffectTraversal));
    }

    public default <E2> FuseGraphTraversal<S, E2> cap(final String sideEffectKey, final String... sideEffectKeys) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.cap, sideEffectKey, sideEffectKeys);
        return this.asAdmin().addStep(new SideEffectCapStep<>(this.asAdmin(), sideEffectKey, sideEffectKeys));
    }

    public default FuseGraphTraversal<S, Edge> subgraph(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.subgraph, sideEffectKey);
        return this.asAdmin().addStep(new SubgraphStep(this.asAdmin(), sideEffectKey));
    }

    public default FuseGraphTraversal<S, E> aggregate(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.aggregate, sideEffectKey);
        return this.asAdmin().addStep(new AggregateStep<>(this.asAdmin(), sideEffectKey));
    }

    public default FuseGraphTraversal<S, E> group(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.group, sideEffectKey);
        return this.asAdmin().addStep(new GroupSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #group(String)}.
     */
    public default FuseGraphTraversal<S, E> groupV3d0(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.groupV3d0, sideEffectKey);
        return this.asAdmin().addStep(new GroupSideEffectStepV3d0<>(this.asAdmin(), sideEffectKey));
    }

    public default FuseGraphTraversal<S, E> groupCount(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.groupCount, sideEffectKey);
        return this.asAdmin().addStep(new GroupCountSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    public default FuseGraphTraversal<S, E> tree(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.tree, sideEffectKey);
        return this.asAdmin().addStep(new TreeSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    public default <V, U> FuseGraphTraversal<S, E> sack(final BiFunction<V, U, V> sackOperator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.sack, sackOperator);
        return this.asAdmin().addStep(new SackValueStep<>(this.asAdmin(), sackOperator));
    }

    /**
     * @deprecated As of release 3.1.0, replaced by {@link #sack(BiFunction)} with {@link #by(String)}.
     */
    @Deprecated
    public default <V, U> FuseGraphTraversal<S, E> sack(final BiFunction<V, U, V> sackOperator, final String elementPropertyKey) {
        return this.sack(sackOperator).by(elementPropertyKey);
    }

    public default FuseGraphTraversal<S, E> store(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.store, sideEffectKey);
        return this.asAdmin().addStep(new StoreStep<>(this.asAdmin(), sideEffectKey));
    }

    public default FuseGraphTraversal<S, E> profile(final String sideEffectKey) {
        this.asAdmin().getBytecode().addStep(Traversal.Symbols.profile, sideEffectKey);
        return this.asAdmin().addStep(new ProfileSideEffectStep<>(this.asAdmin(), sideEffectKey));
    }

    @Override
    public default FuseGraphTraversal<S, TraversalMetrics> profile() {
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
     *                    to use its default settings
     * @param key         the key for the property
     * @param value       the value for the property
     * @param keyValues   any meta properties to be assigned to this property
     */
    public default FuseGraphTraversal<S, E> property(final VertexProperty.Cardinality cardinality, final Object key, final Object value, final Object... keyValues) {
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
     * {@link org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality} is defaulted to {@code null} which
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
    public default FuseGraphTraversal<S, E> property(final Object key, final Object value, final Object... keyValues) {
        return key instanceof VertexProperty.Cardinality ?
                this.property((VertexProperty.Cardinality) key, value, keyValues[0],
                        keyValues.length > 1 ?
                                Arrays.copyOfRange(keyValues, 1, keyValues.length) :
                                new Object[]{}) :
                this.property(null, key, value, keyValues);
    }

    ///////////////////// BRANCH STEPS /////////////////////

    public default <M, E2> FuseGraphTraversal<S, E2> branch(final Traversal<?, M> branchTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.branch, branchTraversal);
        final BranchStep<E, E2, M> branchStep = new BranchStep<>(this.asAdmin());
        branchStep.setBranchTraversal((Traversal.Admin<E, M>) branchTraversal);
        return this.asAdmin().addStep(branchStep);
    }

    public default <M, E2> FuseGraphTraversal<S, E2> branch(final Function<Traverser<E>, M> function) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.branch, function);
        final BranchStep<E, E2, M> branchStep = new BranchStep<>(this.asAdmin());
        branchStep.setBranchTraversal((Traversal.Admin<E, M>) __.map(function));
        return this.asAdmin().addStep(branchStep);
    }

    public default <M, E2> FuseGraphTraversal<S, E2> choose(final Traversal<?, M> choiceTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choiceTraversal);
        return this.asAdmin().addStep(new ChooseStep<>(this.asAdmin(), (Traversal.Admin<E, M>) choiceTraversal));
    }

    public default <E2> FuseGraphTraversal<S, E2> choose(final Traversal<?, ?> traversalPredicate,
                                                     final Traversal<?, E2> trueChoice, final Traversal<?, E2> falseChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, traversalPredicate, trueChoice, falseChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) traversalPredicate, (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) falseChoice));
    }

    public default <E2> FuseGraphTraversal<S, E2> choose(final Traversal<?, ?> traversalPredicate,
                                                     final Traversal<?, E2> trueChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, traversalPredicate, trueChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) traversalPredicate, (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) __.identity()));
    }

    public default <M, E2> FuseGraphTraversal<S, E2> choose(final Function<E, M> choiceFunction) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choiceFunction);
        return this.asAdmin().addStep(new ChooseStep<>(this.asAdmin(), (Traversal.Admin<E, M>) __.map(new FunctionTraverser<>(choiceFunction))));
    }

    public default <E2> FuseGraphTraversal<S, E2> choose(final Predicate<E> choosePredicate,
                                                     final Traversal<?, E2> trueChoice, final Traversal<?, E2> falseChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choosePredicate, trueChoice, falseChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) __.filter(new PredicateTraverser<>(choosePredicate)), (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) falseChoice));
    }

    public default <E2> FuseGraphTraversal<S, E2> choose(final Predicate<E> choosePredicate,
                                                     final Traversal<?, E2> trueChoice) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.choose, choosePredicate, trueChoice);
        return this.asAdmin().addStep(new ChooseStep<E, E2, Boolean>(this.asAdmin(), (Traversal.Admin<E, ?>) __.filter(new PredicateTraverser<>(choosePredicate)), (Traversal.Admin<E, E2>) trueChoice, (Traversal.Admin<E, E2>) __.identity()));
    }

    public default <E2> FuseGraphTraversal<S, E2> optional(final Traversal<?, E2> optionalTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.optional, optionalTraversal);
        return this.asAdmin().addStep(new ChooseStep<>(this.asAdmin(), (Traversal.Admin<E, ?>) optionalTraversal, (Traversal.Admin<E, E2>) optionalTraversal.asAdmin().clone(), (Traversal.Admin<E, E2>) __.<E2>identity()));
    }

    public default <E2> FuseGraphTraversal<S, E2> union(final Traversal<?, E2>... unionTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.union, unionTraversals);
        return this.asAdmin().addStep(new UnionStep(this.asAdmin(), Arrays.copyOf(unionTraversals, unionTraversals.length, Traversal.Admin[].class)));
    }

    public default <E2> FuseGraphTraversal<S, E2> coalesce(final Traversal<?, E2>... coalesceTraversals) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.coalesce, coalesceTraversals);
        return this.asAdmin().addStep(new CoalesceStep(this.asAdmin(), Arrays.copyOf(coalesceTraversals, coalesceTraversals.length, Traversal.Admin[].class)));
    }

    public default FuseGraphTraversal<S, E> repeat(final Traversal<?, E> repeatTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.repeat, repeatTraversal);
        return RepeatStep.addRepeatToTraversal(this, (Traversal.Admin<E, E>) repeatTraversal);
    }

    public default FuseGraphTraversal<S, E> emit(final Traversal<?, ?> emitTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.emit, emitTraversal);
        return RepeatStep.addEmitToTraversal(this, (Traversal.Admin<E, ?>) emitTraversal);
    }

    public default FuseGraphTraversal<S, E> emit(final Predicate<Traverser<E>> emitPredicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.emit, emitPredicate);
        return RepeatStep.addEmitToTraversal(this, (Traversal.Admin<E, ?>) __.filter(emitPredicate));
    }

    public default FuseGraphTraversal<S, E> emit() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.emit);
        return RepeatStep.addEmitToTraversal(this, TrueTraversal.instance());
    }

    public default FuseGraphTraversal<S, E> until(final Traversal<?, ?> untilTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.until, untilTraversal);
        return RepeatStep.addUntilToTraversal(this, (Traversal.Admin<E, ?>) untilTraversal);
    }

    public default FuseGraphTraversal<S, E> until(final Predicate<Traverser<E>> untilPredicate) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.until, untilPredicate);
        return RepeatStep.addEmitToTraversal(this, (Traversal.Admin<E, ?>) __.filter(untilPredicate));
    }

    public default FuseGraphTraversal<S, E> times(final int maxLoops) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.times, maxLoops);
        if (this.asAdmin().getEndStep() instanceof TimesModulating) {
            ((TimesModulating) this.asAdmin().getEndStep()).modulateTimes(maxLoops);
            return this;
        } else
            return RepeatStep.addUntilToTraversal(this, new LoopTraversal<>(maxLoops));
    }

    public default <E2> FuseGraphTraversal<S, E2> local(final Traversal<?, E2> localTraversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.local, localTraversal);
        return this.asAdmin().addStep(new LocalStep<>(this.asAdmin(), localTraversal.asAdmin()));
    }

    /////////////////// VERTEX PROGRAM STEPS ////////////////

    public default FuseGraphTraversal<S, E> pageRank() {
        return this.pageRank(0.85d);
    }

    public default FuseGraphTraversal<S, E> pageRank(final double alpha) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.pageRank, alpha);
        return this.asAdmin().addStep((Step<E, E>) new PageRankVertexProgramStep(this.asAdmin(), alpha));
    }

    public default FuseGraphTraversal<S, E> peerPressure() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.peerPressure);
        return this.asAdmin().addStep((Step<E, E>) new PeerPressureVertexProgramStep(this.asAdmin()));
    }

    public default FuseGraphTraversal<S, E> program(final VertexProgram<?> vertexProgram) {
        return this.asAdmin().addStep((Step<E, E>) new ProgramVertexProgramStep(this.asAdmin(), vertexProgram));
    }

    ///////////////////// UTILITY STEPS /////////////////////

    public default FuseGraphTraversal<S, E> as(final String stepLabel, final String... stepLabels) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.as, stepLabel, stepLabels);
        if (this.asAdmin().getSteps().size() == 0) this.asAdmin().addStep(new StartStep<>(this.asAdmin()));
        final Step<?, E> endStep = this.asAdmin().getEndStep();
        endStep.addLabel(stepLabel);
        for (final String label : stepLabels) {
            endStep.addLabel(label);
        }
        return this;
    }

    public default FuseGraphTraversal<S, E> barrier() {
        return this.barrier(Integer.MAX_VALUE);
    }

    public default FuseGraphTraversal<S, E> barrier(final int maxBarrierSize) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.barrier, maxBarrierSize);
        return this.asAdmin().addStep(new NoOpBarrierStep<>(this.asAdmin(), maxBarrierSize));
    }

    public default FuseGraphTraversal<S, E> barrier(final Consumer<TraverserSet<Object>> barrierConsumer) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.barrier, barrierConsumer);
        return this.asAdmin().addStep(new LambdaCollectingBarrierStep<>(this.asAdmin(), (Consumer) barrierConsumer, Integer.MAX_VALUE));
    }


    //// BY-MODULATORS

    public default FuseGraphTraversal<S, E> by() {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy();
        return this;
    }

    public default FuseGraphTraversal<S, E> by(final Traversal<?, ?> traversal) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, traversal);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(traversal.asAdmin());
        return this;
    }

    public default FuseGraphTraversal<S, E> by(final T token) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, token);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(token);
        return this;
    }

    public default FuseGraphTraversal<S, E> by(final String key) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, key);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(key);
        return this;
    }

    public default <V> FuseGraphTraversal<S, E> by(final Function<V, Object> function) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, function);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(function);
        return this;
    }

    //// COMPARATOR BY-MODULATORS

    public default <V> FuseGraphTraversal<S, E> by(final Traversal<?, ?> traversal, final Comparator<V> comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, traversal, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(traversal.asAdmin(), comparator);
        return this;
    }

    public default FuseGraphTraversal<S, E> by(final Comparator<E> comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(comparator);
        return this;
    }

    public default FuseGraphTraversal<S, E> by(final Order order) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, order);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(order);
        return this;
    }

    public default <V> FuseGraphTraversal<S, E> by(final String key, final Comparator<V> comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, key, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(key, comparator);
        return this;
    }

    /*public default <V> GraphTraversal<S, E> by(final Column column, final Comparator<V> comparator) {
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(column, comparator);
        return this;
    }

    public default <V> GraphTraversal<S, E> by(final T token, final Comparator<V> comparator) {
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(token, comparator);
        return this;
    }*/

    public default <U> FuseGraphTraversal<S, E> by(final Function<U, Object> function, final Comparator comparator) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.by, function, comparator);
        ((ByModulating) this.asAdmin().getEndStep()).modulateBy(function, comparator);
        return this;
    }

    ////

    public default <M, E2> FuseGraphTraversal<S, E> option(final M pickToken, final Traversal<E, E2> traversalOption) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.option, pickToken, traversalOption);
        ((TraversalOptionParent<M, E, E2>) this.asAdmin().getEndStep()).addGlobalChildOption(pickToken, traversalOption.asAdmin());
        return this;
    }

    public default <E2> FuseGraphTraversal<S, E> option(final Traversal<E, E2> traversalOption) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.option, traversalOption);
        return this.option(TraversalOptionParent.Pick.any, traversalOption.asAdmin());
    }

    ////

    @Override
    public default FuseGraphTraversal<S, E> iterate() {
        GraphTraversal.super.iterate();
        return this;
    }

    ////

    public static final class Symbols {

        private Symbols() {
            // static fields only
        }

        public static final String join = "join";
    }
}
