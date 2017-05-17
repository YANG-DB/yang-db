package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologyGraphLayoutProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.elasticsearch.common.collect.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 5/17/2017.
 */
public class ScenarioMockUtil {
    private Ontology ontology;
    private PhysicalIndexProvider indexProvider;
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private OntologyGraphLayoutProvider ontologyGraphLayoutProvider = null;
    private Map<String, Map<String, String>> redundantProps = new HashMap<>();
    private Map<Tuple<String, ElementType>, IndexPartition> indexPartitionMap = new HashMap<>();

    public ScenarioMockUtil() {
        this.ontologyGraphLayoutProvider = mock(OntologyGraphLayoutProvider.class);
        when(this.ontologyGraphLayoutProvider.getRedundantVertexProperty(any(), any())).thenAnswer(invocationOnMock -> {
            String edgeType = invocationOnMock.getArgumentAt(0, String.class);
            String property = invocationOnMock.getArgumentAt(1, String.class);
            if(redundantProps.containsKey(edgeType)){
                if(redundantProps.get(edgeType).containsKey(property)){
                    return Optional.of(redundantProps.get(edgeType).get(property));
                }
            }
            return Optional.empty();
        });

        when(this.ontologyGraphLayoutProvider.getRedundantVertexPropertyByPushdownName(any(), any())).thenAnswer(invocationOnMock -> {
            String edgeType = invocationOnMock.getArgumentAt(0, String.class);
            String property = invocationOnMock.getArgumentAt(1, String.class);
            if(redundantProps.containsKey(edgeType)){
                if(redundantProps.get(edgeType).containsValue(property)){
                    return redundantProps.get(edgeType).entrySet().stream().filter(set -> set.getValue().equals(property)).map(Map.Entry::getKey).findFirst();
                }
            }
            return Optional.empty();
        });

        this.ontology = OntologyTestUtils.createDragonsOntologyShort();

        this.indexProvider = mock(PhysicalIndexProvider.class);
        when(this.indexProvider.getIndexPartitionByLabel(any(), any())).thenAnswer(invocationOnMock -> {
            String label = invocationOnMock.getArgumentAt(0, String.class);
            ElementType elementType = invocationOnMock.getArgumentAt(1, ElementType.class);
            Tuple<String, ElementType> item = new Tuple<>(label, elementType);
            return indexPartitionMap.getOrDefault(item, null);
        });

        this.graphElementSchemaProvider = new OntologySchemaProvider(this.indexProvider, this.ontology, this.ontologyGraphLayoutProvider);
    }

    public ScenarioMockUtil withLayoutRedundancy(String edgeType, String propertyName, String redundantPropertyName){
        if(!redundantProps.containsKey(edgeType)){
            redundantProps.put(edgeType, new HashMap<>());
        }
        redundantProps.get(edgeType).put(propertyName, redundantPropertyName);
        return this;
    }

    public static ScenarioMockUtil start(){
        return new ScenarioMockUtil();
    }
}
