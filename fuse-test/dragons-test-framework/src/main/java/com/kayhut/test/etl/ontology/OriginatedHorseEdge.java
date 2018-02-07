package com.kayhut.test.etl.ontology;

import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.test.etl.*;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.test.scenario.ETLUtils.*;

/**
 * Created by moti on 6/7/2017.
 */
public interface OriginatedHorseEdge {
    static void main(String args[]) throws IOException {
        Map<String, String> outConstFields=  new HashMap<>();
        outConstFields.put(ENTITY_A_TYPE, HORSE);
        outConstFields.put(ENTITY_B_TYPE, KINGDOM);
        AddConstantFieldsTransformer outFieldsTransformer = new AddConstantFieldsTransformer(outConstFields, R);
        Map<String, String> inConstFields=  new HashMap<>();
        inConstFields.put(ENTITY_A_TYPE, KINGDOM);
        inConstFields.put(ENTITY_B_TYPE, HORSE);
        AddConstantFieldsTransformer inFieldsTransformer = new AddConstantFieldsTransformer(inConstFields, L);

        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(getClient(),
                redundant(ORIGINATED, R, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(HORSE).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                HORSE,
                redundant(ORIGINATED, R,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                KINGDOM,
                R.translatedName());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(getClient(),
                redundant(ORIGINATED,  L, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                KINGDOM,
                redundant(ORIGINATED, L,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(HORSE).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                HORSE, Rel.Direction.L.translatedName());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(SINCE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, ORIGINATED + "H");
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                duplicateEdgeTransformer,
                outFieldsTransformer,
                inFieldsTransformer,
                redundantInTransformer,
                redundantOutTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_HORSE.csv",
                "C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_HORSE-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_A_ID, ENTITY_B_ID,  SINCE),
                5000);
        transformer.transform();
    }

}
