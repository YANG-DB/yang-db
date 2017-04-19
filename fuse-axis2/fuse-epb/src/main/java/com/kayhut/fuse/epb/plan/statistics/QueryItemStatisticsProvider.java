package com.kayhut.fuse.epb.plan.statistics;

import com.google.inject.Inject;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;

import java.util.*;

/**
 * Created by moti on 4/12/2017.
 */
public class QueryItemStatisticsProvider implements StatisticsProvider<EBase> {
    private StatisticsProvider<RawGraphStatisticableItemInfo> rawGraphStatisticsProvider;
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology ontology;
    //private OntologySchemaProvider ontologySchemaProvider;

    @Inject
    public QueryItemStatisticsProvider(StatisticsProvider<RawGraphStatisticableItemInfo> rawGraphStatisticsProvider,
                                       GraphElementSchemaProvider graphElementSchemaProvider,
                                       Ontology ontology){
        this.rawGraphStatisticsProvider = rawGraphStatisticsProvider;
        this.graphElementSchemaProvider = graphElementSchemaProvider;
        this.ontology = ontology;
        //this.ontologySchemaProvider = ontologySchemaProvider;
    }

    @Override
    public Statistics getStatistics(EBase item) {
        if(item instanceof EConcrete){
            return new Statistics.CardinalityStatistics(1,1);
        }

        if(item instanceof ETyped){
            String eTypeName = OntologyUtil.getEntityTypeNameById(ontology, ((ETyped) item).geteType());
            GraphVertexSchema graphVertexSchema = graphElementSchemaProvider.getVertexSchema(eTypeName).get();
            RawGraphStatisticableItemInfo itemInfo = new RawGraphStatisticableItemInfo(graphVertexSchema, new Condition[0]);
            return rawGraphStatisticsProvider.getStatistics(itemInfo);
        }

        if(item instanceof EUntyped){
            Statistics statistics = null;
            EUntyped eUntyped = (EUntyped) item;
            List<String> vertexTypes = new LinkedList<>();
            if(eUntyped.getvTypes().size() > 0){
                for(int vType : eUntyped.getvTypes()){
                    vertexTypes.add(OntologyUtil.getEntityTypeNameById(ontology, vType));
                }
            }else if (eUntyped.getNvTypes().size() > 0){
                graphElementSchemaProvider.getVertexTypes().forEach(v -> vertexTypes.add(v));
                for(int vType : eUntyped.getNvTypes()){
                    vertexTypes.remove(OntologyUtil.getEntityTypeNameById(ontology, vType));
                }
            }else{
                graphElementSchemaProvider.getVertexTypes().forEach(v -> vertexTypes.add(v));
            }
            for(String vertexType : vertexTypes) {
                GraphVertexSchema graphVertexSchema = graphElementSchemaProvider.getVertexSchema(vertexType).get();
                RawGraphStatisticableItemInfo itemInfo = new RawGraphStatisticableItemInfo(graphVertexSchema, new Condition[0]);
                Statistics currStatistics = rawGraphStatisticsProvider.getStatistics(itemInfo);
                statistics = currStatistics.merge(statistics);
            }
            return statistics;
        }

        if(item instanceof Rel){
            graphElementSchemaProvider.getEdgeSchema(OntologyUtil.getRelationTypeNameById(ontology, ((Rel) item).getrType()), Optional.empty(), Optional.empty());

        }





        return null;
    }
}
