package com.yangdb.fuse.asg.translator.cypher.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.yangdb.fuse.asg.translator.cypher.strategies.expressions.EqualityExpression;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.opencypher.v9_0.expressions.*;
import org.opencypher.v9_0.util.InputPosition;
import scala.Option;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.maxEntityNum;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class StepPatternCypherTranslatorStrategy implements CypherElementTranslatorStrategy<PatternElement> {


    public StepPatternCypherTranslatorStrategy(NodePatternCypherTranslatorStrategy nodePattern, EqualityExpression equalityExpression) {
        this.nodePattern = nodePattern;
        this.equalityExpression = equalityExpression;

    }

    @Override
    public void apply(PatternElement element, AsgQuery query, CypherStrategyContext context) {
        if (element instanceof RelationshipChain) {
            // node variables this chain shares with earlier comma-separated patterns
            final Set<String> boundBefore = nodeNames((RelationshipChain) element).stream()
                    .filter(name -> AsgQueryUtil.getByTag(query.getStart(), name).isPresent())
                    .collect(Collectors.toSet());
            applyChain((RelationshipChain) element, query, context, boundBefore, new ArrayList<>());
        }
    }

    private void applyChain(RelationshipChain chain, AsgQuery query, CypherStrategyContext context,
                            Set<String> boundBefore, List<String> chainNodes) {
        final PatternElement left = chain.element();
        if (left instanceof RelationshipChain) applyChain((RelationshipChain) left, query, context, boundBefore, chainNodes);
        if (left instanceof NodePattern) {
            nodePattern.apply(left, query, context);
            name((NodePattern) left).ifPresent(chainNodes::add);
        }

        final RelationshipPattern relationship = chain.relationship();
        final NodePattern right = chain.rightNode();
        final Optional<String> rightName = name(right);
        if (rightName.isPresent() && AsgQueryUtil.getByTag(query.getStart(), rightName.get()).isPresent()) {
            // the ASG is a tree: a relationship can't end on a node that already has a place in it
            throw closingOnBoundNode(relationship, rightName.get(),
                    chainNodes.contains(rightName.get()) || chainNodes.stream().anyMatch(boundBefore::contains));
        }
        apply(relationship, query, context);
        this.nodePattern.apply(right, query, context);
        rightName.ifPresent(chainNodes::add);
    }

    private static FuseError.FuseErrorException closingOnBoundNode(RelationshipPattern relationship, String node, boolean cyclic) {
        final String rel = relationship.variable().isEmpty() ? "a relationship" : "relationship [" + relationship.variable().get().name() + "]";
        final String description = cyclic
                ? String.format("Cyclic patterns are not supported: %s ends on (%s), which the pattern already reaches. " +
                "Only tree-shaped patterns can be matched; drop one relationship of the cycle.", rel, node)
                : String.format("A pattern can't end on the already-matched node (%s): %s would be left without an end node. " +
                "Write this pattern starting from (%s) instead, reversing its direction.", node, rel, node);
        return new FuseError.FuseErrorException("Unsupported Cypher pattern", new FuseError("Unsupported Cypher pattern", description));
    }

    /**
     * Q9: a relationship matches a single type. Keeping only the first of {@code [r:A|B]} would silently drop results,
     * so a type union is rejected until it is supported.
     */
    private static FuseError.FuseErrorException relationshipTypeUnion(String variable, List<String> types) {
        final String pattern = "[" + variable + ":" + String.join("|", types) + "]";
        final String description = String.format("Relationship type union such as %s is not supported: a relationship " +
                        "pattern can have only one type. Run one query per type instead (%s) and combine the results.",
                pattern, types.stream().map(t -> "[" + variable + ":" + t + "]").collect(Collectors.joining(", ")));
        return new FuseError.FuseErrorException("Unsupported Cypher syntax", new FuseError("Unsupported Cypher syntax", description));
    }

    private static Optional<String> name(NodePattern node) {
        return node.variable().isEmpty() ? Optional.empty() : Optional.of(node.variable().get().name());
    }

    private static List<String> nodeNames(RelationshipChain chain) {
        final List<String> names = new ArrayList<>();
        final PatternElement left = chain.element();
        if (left instanceof RelationshipChain) names.addAll(nodeNames((RelationshipChain) left));
        if (left instanceof NodePattern) name((NodePattern) left).ifPresent(names::add);
        name(chain.rightNode()).ifPresent(names::add);
        return names;
    }


    public void apply(RelationshipPattern element, AsgQuery query, CypherStrategyContext context) {
        final Option<LogicalVariable> variable = element.variable();

        int current = maxEntityNum(query)+1;
        String name = "Rel_#" + current;

        if (!variable.isEmpty()) {
            //get scope and calculate next enum
            final LogicalVariable logicalVariable = variable.get();
            name = logicalVariable.name();
        }

        //labels
        Collection<RelTypeName> labels = asJavaCollectionConverter((element).types()).asJavaCollection();
        final List<String> rTypes = labels.stream().map(l -> l.name()).collect(Collectors.toList());
        if (rTypes.size() > 1) {
            throw relationshipTypeUnion(variable.isEmpty() ? "" : variable.get().name(), rTypes);
        }

        final SemanticDirection direction = element.direction();

        //build label and update query, mutate new current scope
        AsgEBase<? extends EBase> quant = CypherUtils.quant(context.getScope(), Optional.empty(), query, context);
        context.scope(quant);

        AsgEBase<Rel> rel = new AsgEBase<>(new Rel(current, rTypes.isEmpty() ? "*" : rTypes.get(0), resolve(direction), name, current + 1, 0));

        context.getScope().addNext(rel);
        context.scope(rel);

        final Option<Expression> properties = element.properties();
        if (properties.nonEmpty()) {
            addProps(variable.isEmpty() ? new Variable(name, InputPosition.NONE()) : variable.get(),
                    (MapExpression) properties.get(), query, context);
        }

        context.scope(rel);
    }

    /**
     * Each {@code {k: v}} entry of the pattern becomes {@code rel.k = v}, translated exactly like the same equality in
     * a WHERE clause: one RelProp per entry, in the relationship's RelPropGroup. An unnamed relationship is addressed
     * by its generated tag.
     */
    private void addProps(LogicalVariable relVariable, MapExpression map, AsgQuery query, CypherStrategyContext context) {
        Collection<Tuple2<PropertyKeyName, Expression>> items = asJavaCollectionConverter(map.items()).asJavaCollection();
        for (Tuple2<PropertyKeyName, Expression> item : items) {
            Property property = new Property(relVariable, item._1, InputPosition.NONE());
            Equals equals = new Equals(property, item._2, InputPosition.NONE());
            equalityExpression.apply(Optional.empty(),
                    com.bpodgursky.jbool_expressions.Variable.of(CypherUtils.Wrapper.of(equals)), query, context);
        }
    }

    private Rel.Direction resolve(SemanticDirection direction) {
        if (direction instanceof SemanticDirection.INCOMING$)
            return Rel.Direction.L;
        if (direction instanceof SemanticDirection.OUTGOING$)
            return Rel.Direction.R;
        return Rel.Direction.RL;
    }

    private NodePatternCypherTranslatorStrategy nodePattern;
    private final EqualityExpression equalityExpression;

}
