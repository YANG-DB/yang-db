package com.kayhut.test.etl.ontology;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.test.etl.*;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.model.execution.plan.Direction.out;
import static com.kayhut.test.scenario.ETLUtils.*;

/**
 * Created by moti on 6/7/2017.
 */
public interface OriginatedDragonEdge {
    static void main(String args[]) throws IOException {
        Map<String, String> outConstFields=  new HashMap<>();
        outConstFields.put(ENTITY_A_TYPE, DRAGON);
        outConstFields.put(ENTITY_B_TYPE, KINGDOM);
        AddConstantFieldsTransformer outFieldsTransformer = new AddConstantFieldsTransformer(outConstFields, out);
        Map<String, String> inConstFields=  new HashMap<>();
        inConstFields.put(ENTITY_A_TYPE, KINGDOM);
        inConstFields.put(ENTITY_B_TYPE, DRAGON);
        AddConstantFieldsTransformer inFieldsTransformer = new AddConstantFieldsTransformer(inConstFields, Direction.in);

        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(getClient(),
                redundant(ORIGINATED, out, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON,
                redundant(ORIGINATED, out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(KINGDOM).getIndices()).toJavaList(),
                KINGDOM,
                out.name());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(getClient(),
                redundant(ORIGINATED,  Direction.in, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(KINGDOM).getIndices()).toJavaList(),
                KINGDOM,
                redundant(ORIGINATED, Direction.in,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON, Direction.in.name());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, ORIGINATED + "D");
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                duplicateEdgeTransformer,
                outFieldsTransformer,
                inFieldsTransformer,
                redundantInTransformer,
                redundantOutTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_DRAGON.csv",
                "C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_DRAGON-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_B_ID, ENTITY_A_ID,  START_DATE),
                5000);
        transformer.transform();
    }

}
