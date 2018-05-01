package com.kayhut.fuse.asg.strategy.schema;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.utils.LikeUtil;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.BasePropGroup;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.SchematicEProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by roman.margolis on 07/03/2018.
 */
public class LikeAnyConstraintTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public LikeAnyConstraintTransformationAsgStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return;
        }

        Ontology.Accessor ont = new Ontology.Accessor(ontology.get());
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ont.get());

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            //currently supporting only ETyped or EConcrete
            Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EEntityBase.class);
            if (!eTypedAsgEBase.isPresent()) {
                return;
            }

            for (EProp eProp : new ArrayList<>(ePropGroupAsgEBase.geteBase().getProps())) {
                if (!eProp.getCon().getOp().equals(ConstraintOp.likeAny)) {
                    continue;
                }

                Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(eTypedAsgEBase.get().geteBase().geteType());
                if (Stream.ofAll(vertexSchemas).isEmpty()) {
                    continue;
                }

                // currently supports a single vertex schema
                GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

                Optional<Property> property = ont.$property(eProp.getpType());
                if (!property.isPresent()) {
                    continue;
                }

                Optional<GraphElementPropertySchema> propertySchema = vertexSchema.getProperty(property.get().getName());
                if (!propertySchema.isPresent()) {
                    continue;
                }

                EPropGroup newEpropGroup = new EPropGroup(
                        eProp.geteNum(),
                        QuantType.some,
                        Collections.emptyList(),
                        Stream.ofAll(CollectionUtil.listFromObjectValue(eProp.getCon().getExpr()))
                        .map(value -> new EPropGroup(
                                eProp.geteNum(),
                                LikeUtil.applyWildcardRules(
                                    EProp.of(eProp.geteNum(), eProp.getpType(), Constraint.of(ConstraintOp.like, value)),
                                    propertySchema.get()))));

                Map<String, List<EPropGroup>> insetGroups = Stream.ofAll(newEpropGroup.getGroups())
                        .filter(group -> group.getProps().size() == 1)
                        .filter(group -> group.getProps().get(0).getCon().getOp().equals(ConstraintOp.eq))
                        .filter(group -> group.getProps().get(0) instanceof SchematicEProp)
                        .groupBy(group -> group.getProps().get(0).getpType())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1, grouping._2.toJavaList()));

                List<EProp> insetProps = Stream.ofAll(insetGroups.entrySet())
                        .<EProp>map(entry -> new SchematicEProp(
                                0,
                                entry.getKey(),
                                ((SchematicEProp)entry.getValue().get(0).getProps().get(0)).getSchematicName(),
                                Constraint.of(
                                        ConstraintOp.inSet,
                                        Stream.ofAll(entry.getValue()).map(group -> group.getProps().get(0).getCon().getExpr().toString()).toJavaList())))
                        .toJavaList();

                Stream.ofAll(insetGroups.values()).flatMap(groups -> groups).forEach(group -> newEpropGroup.getGroups().remove(group));
                newEpropGroup.getProps().addAll(insetProps);

                ePropGroupAsgEBase.geteBase().getProps().remove(eProp);
                ePropGroupAsgEBase.geteBase().getGroups().add(newEpropGroup);
            }
        });
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
