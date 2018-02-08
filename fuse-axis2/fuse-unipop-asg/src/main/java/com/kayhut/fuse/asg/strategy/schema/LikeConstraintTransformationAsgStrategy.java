package com.kayhut.fuse.asg.strategy.schema;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.SchematicEProp;
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
public class LikeConstraintTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public LikeConstraintTransformationAsgStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
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
                if (!eProp.getCon().getOp().equals(ConstraintOp.like)) {
                    continue;
                }

                Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(eTypedAsgEBase.get().geteBase().geteType());
                if (!vertexSchema.isPresent()) {
                    continue;
                }

                Optional<Property> property = ont.$property(eProp.getpType());
                if (!property.isPresent()) {
                    continue;
                }

                Optional<GraphElementPropertySchema> propertySchema = vertexSchema.get().getProperty(property.get().getName());
                if (!propertySchema.isPresent()) {
                    continue;
                }

                applyWildcardRules(ePropGroupAsgEBase.geteBase(), eProp, propertySchema.get());
            }

        });
    }
    //endregion

    //region Private Methods
    private void applyWildcardRules(EPropGroup ePropGroup, EProp eProp, GraphElementPropertySchema propertySchema) {
        ePropGroup.getProps().remove(eProp);

        Optional<GraphElementPropertySchema.ExactIndexingSchema> exactIndexingSchema = propertySchema.getIndexingSchema(exact);

        String expr = (String) eProp.getCon().getExpr();
        if (expr == null || expr.equals("")) {
            ePropGroup.getProps().add(new SchematicEProp(
                    eProp.geteNum(),
                    eProp.getpType(),
                    exactIndexingSchema.get().getName(),
                    Constraint.of(ConstraintOp.eq, eProp.getCon().getExpr())));
            return;
        }

        List<String> wildcardParts = Stream.of(expr.split("\\*")).filter(part -> !part.equals("")).toJavaList();

        boolean prefix = !expr.startsWith("*");
        boolean suffix = !expr.endsWith("*");
        boolean exact = prefix && suffix && wildcardParts.size() == 1;

        if (exact) {
            ePropGroup.getProps().add(new SchematicEProp(
                    eProp.geteNum(),
                    eProp.getpType(),
                    exactIndexingSchema.get().getName(),
                    Constraint.of(ConstraintOp.eq, eProp.getCon().getExpr())));
            return;
        }

        for (int wildcardPartIndex = 0; wildcardPartIndex < wildcardParts.size(); wildcardPartIndex++) {
            String wildcardPart = wildcardParts.get(wildcardPartIndex);

            if (wildcardPartIndex == 0 && prefix) {
                ePropGroup.getProps().add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, wildcardParts.get(0) + "*")));

            } else if (wildcardPartIndex == wildcardParts.size() - 1 && suffix) {
                ePropGroup.getProps().add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, "*" + wildcardParts.get(wildcardParts.size() - 1))));

            } else if (ngramsApplicable(eProp, propertySchema, wildcardPart)) {
                ePropGroup.getProps().add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        propertySchema.getIndexingSchema(ngrams).get().getName(),
                        Constraint.of(ConstraintOp.eq, wildcardPart)));

            } else {
                ePropGroup.getProps().add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, "*" + wildcardParts.get(wildcardPartIndex) + "*")));
            }
        }
    }

    private boolean ngramsApplicable(EProp eProp, GraphElementPropertySchema propertySchema, String wildcardPart) {
        Optional<GraphElementPropertySchema.NgramsIndexingSchema> ngramsIndexingSchema = propertySchema.getIndexingSchema(ngrams);

        if (!wildcardPart.contains(" ") &&
                ngramsIndexingSchema.isPresent() &&
                wildcardPart.length() <= (ngramsIndexingSchema.get()).getMaxSize()) {
            return true;
        }

        return false;
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
