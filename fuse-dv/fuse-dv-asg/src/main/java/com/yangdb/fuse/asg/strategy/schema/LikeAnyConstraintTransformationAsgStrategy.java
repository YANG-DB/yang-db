package com.yangdb.fuse.asg.strategy.schema;

/*-
 * #%L
 * fuse-dv-asg
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.asg.strategy.AsgElementStrategy;
import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.asg.strategy.schema.utils.LikeUtil;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.BasePropGroup;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.SchematicEProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.unipop.controller.utils.CollectionUtil;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;

import java.util.*;

import static com.yangdb.fuse.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.*;

/**
 * Created by roman.margolis on 07/03/2018.
 */
public class LikeAnyConstraintTransformationAsgStrategy implements AsgStrategy , AsgElementStrategy<EPropGroup> {
    //region Constructors
    public LikeAnyConstraintTransformationAsgStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> apply(query, ePropGroupAsgEBase, context));
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;

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
            if (!eProp.isConstraint() || !eProp.getCon().getOp().equals(ConstraintOp.likeAny)) {
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

            EPropGroup wildcardRuleGroup = new EPropGroup(
                    eProp.geteNum(),
                    QuantType.some,
                    Collections.emptyList(),
                    Stream.ofAll(CollectionUtil.listFromObjectValue(eProp.getCon().getExpr()))
                            .map(value -> new EPropGroup(
                                    eProp.geteNum(),
                                    LikeUtil.applyWildcardRules(
                                            EProp.of(eProp.geteNum(), eProp.getpType(), Constraint.of(ConstraintOp.like, value)),
                                            propertySchema.get()))));

            List<String> ngramWords =
                    Stream.ofAll(wildcardRuleGroup.getGroups())
                            .flatMap(BasePropGroup::getProps)
                            .flatMap(prop -> Stream.ofAll(LikeUtil.getWildcardNgrams(propertySchema.get(), prop.getCon().getExpr().toString())))
                            .distinct()
                            .toJavaList();

            Optional<EProp> ngramsOptimizationEprop =  propertySchema.get().getIndexingSchema(ngrams).isPresent() && !ngramWords.isEmpty()?
                    Optional.of(new SchematicEProp(
                            0,
                            eProp.getpType(),
                            propertySchema.get().getIndexingSchema(ngrams).get().getName(),
                            Constraint.of(ConstraintOp.inSet, ngramWords))) : Optional.empty();

            List<EPropGroup> simpleWildcardGroups = propertySchema.get().getIndexingSchema(ngrams).isPresent() ?
                    Stream.ofAll(wildcardRuleGroup.getGroups())
                            .filter(group -> group.getProps().size() == 1)
                            .filter(group -> group.getProps().get(0).getCon().getOp().equals(ConstraintOp.eq))
                            .filter(group -> group.getProps().get(0) instanceof SchematicEProp)
                            .filter(group -> ((SchematicEProp) group.getProps().get(0)).getSchematicName()
                                    .equals(propertySchema.get().getIndexingSchema(ngrams).get().getName()))
                            .toJavaList() : Collections.emptyList();

            if (!simpleWildcardGroups.isEmpty()) {
                wildcardRuleGroup.getProps().add(new SchematicEProp(
                        0,
                        eProp.getpType(),
                        propertySchema.get().getIndexingSchema(ngrams).get().getName(),
                        Constraint.of(
                                ConstraintOp.inSet,
                                Stream.ofAll(simpleWildcardGroups).map(group -> group.getProps().get(0).getCon().getExpr().toString()).toJavaList())));
            }
            simpleWildcardGroups.forEach(simpleWildcardGroup -> wildcardRuleGroup.getGroups().remove(simpleWildcardGroup));

            ePropGroupAsgEBase.geteBase().getProps().remove(eProp);

            ngramsOptimizationEprop.ifPresent(ngramsEprop -> ePropGroupAsgEBase.geteBase().getProps().add(ngramsEprop));

            if (!wildcardRuleGroup.getGroups().isEmpty() || !wildcardRuleGroup.getProps().isEmpty()) {
                ePropGroupAsgEBase.geteBase().getGroups().add(wildcardRuleGroup);
            }
        }
    }
    //endregion
}
