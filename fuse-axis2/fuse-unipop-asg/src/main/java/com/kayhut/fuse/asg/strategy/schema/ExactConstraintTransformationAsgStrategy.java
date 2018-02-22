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
import java.util.Optional;
import java.util.Set;

import static com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.exact;

/**
 * Created by roman.margolis on 08/02/2018.
 */
public class ExactConstraintTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public ExactConstraintTransformationAsgStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;

        this.includedOps = Stream.of(ConstraintOp.eq, ConstraintOp.ne, ConstraintOp.gt,
                ConstraintOp.ge, ConstraintOp.lt, ConstraintOp.le, ConstraintOp.empty, ConstraintOp.notEmpty,
                ConstraintOp.inSet, ConstraintOp.notInSet, ConstraintOp.inRange, ConstraintOp.notInRange, ConstraintOp.likeAny)
                .toJavaSet();
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
                if (!this.includedOps.contains(eProp.getCon().getOp())) {
                    continue;
                }

                Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(eTypedAsgEBase.get().geteBase().geteType());
                if (Stream.ofAll(vertexSchemas).isEmpty()) {
                    continue;
                }

                // currently supporting a single vertex schema
                GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

                Optional<Property> property = ont.$property(eProp.getpType());
                if (!property.isPresent()) {
                    continue;
                }

                Optional<GraphElementPropertySchema> propertySchema = vertexSchema.getProperty(property.get().getName());
                if (!propertySchema.isPresent()) {
                    continue;
                }

                Optional<GraphElementPropertySchema.ExactIndexingSchema> exactIndexingSchema = propertySchema.get().getIndexingSchema(exact);
                if (!exactIndexingSchema.isPresent()) {
                    // should throw an error?
                    throw new IllegalStateException("should have exact schema index");
                }

                ePropGroupAsgEBase.geteBase().getProps().remove(eProp);
                ePropGroupAsgEBase.geteBase().getProps().add(new SchematicEProp(
                        eProp.geteNum(),
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        eProp.getCon()));
            }
        });
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;

    private Set<ConstraintOp> includedOps;
    //endregion
}
