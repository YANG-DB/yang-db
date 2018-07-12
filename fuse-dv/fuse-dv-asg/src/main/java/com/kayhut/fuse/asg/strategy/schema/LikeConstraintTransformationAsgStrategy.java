package com.kayhut.fuse.asg.strategy.schema;

import com.kayhut.fuse.asg.strategy.AsgElementStrategy;
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
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.SchematicEProp;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.*;


/**
 * Created by roman.margolis on 05/02/2018.
 */
public class LikeConstraintTransformationAsgStrategy implements AsgStrategy, AsgElementStrategy<EPropGroup> {
    //region Constructors
    public LikeConstraintTransformationAsgStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            apply(query, ePropGroupAsgEBase, context);
        });
    }
    //endregion

    //region AsgElementStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgEBase<EPropGroup> ePropGroupAsgEBase, AsgStrategyContext context) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return;
        }

        Ontology.Accessor ont = new Ontology.Accessor(ontology.get());
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ont.get());
        //currently supporting only ETyped or EConcrete
        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EEntityBase.class);
        if (!eTypedAsgEBase.isPresent()) {
            return;
        }

        for (EProp eProp : new ArrayList<>(ePropGroupAsgEBase.geteBase().getProps())) {

            if (eProp.getCon() == null || !eProp.getCon().getOp().equals(ConstraintOp.like)) {
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

            Iterable<EProp> wildcardRuleEprops = LikeUtil.applyWildcardRules(eProp, propertySchema.get());
            List<EProp> ngramEprops = Stream.ofAll(wildcardRuleEprops)
                    .filter(wildcardEprop -> wildcardEprop.getCon().getOp().equals(ConstraintOp.like))
                    .map(wildcardEprop -> LikeUtil.getWildcardNgramsInsetProp(wildcardEprop, propertySchema.get()))
                    .filter(Optional::isPresent)
                    .map(ngramsEprop -> (SchematicEProp)ngramsEprop.get())
                    .flatMap(ngramsEprop -> Stream.ofAll(CollectionUtil.listFromObjectValue(ngramsEprop.getCon().getExpr()))
                            .map(word -> (EProp)new SchematicEProp(
                                    0,
                                    ngramsEprop.getpType(),
                                    ngramsEprop.getSchematicName(),
                                    Constraint.of(ConstraintOp.eq, word)
                                    )))
                    .appendAll(Stream.ofAll(wildcardRuleEprops)
                        .filter(eprop -> eprop.getCon().getOp().equals(ConstraintOp.eq)))
                    .toJavaList();

            ePropGroupAsgEBase.geteBase().getProps().remove(eProp);
            ePropGroupAsgEBase.geteBase().getProps().addAll(ngramEprops);
            ePropGroupAsgEBase.geteBase().getProps().addAll(
                    Stream.ofAll(wildcardRuleEprops)
                            .filter(eprop -> eprop.getCon().getOp().equals(ConstraintOp.like))
                            .toJavaList());
        }
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
