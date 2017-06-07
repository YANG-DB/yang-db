package com.kayhut.test.etl.ontology;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.test.etl.*;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;

import static com.kayhut.test.scenario.ETLUtils.*;

/**
 * Created by moti on 6/7/2017.
 */
public interface FireEdge {
    static void main(String args[]) throws IOException {

        // Fires
        // Add sideB type
        // redundant field
        // dup + add direction

        AddConstantFieldTransformer entityBFieldTransformer = new AddConstantFieldTransformer(ENTITY_B_TYPE, DRAGON);
        AddConstantFieldTransformer entityAFieldTransformer = new AddConstantFieldTransformer(ENTITY_A_TYPE, DRAGON);
        RedundantFieldTransformer redundantFieldTransformer = new RedundantFieldTransformer(getClient(),
                redundant(FIRE, Direction.out,"A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON,
                redundant(FIRE, Direction.out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON);
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, FIRE);
        ChainedTransformer chainedTransformer = new ChainedTransformer(entityAFieldTransformer,
                entityBFieldTransformer,
                duplicateEdgeTransformer,
                redundantFieldTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\dragonsRelations_FIRES.csv",
                "C:\\demo_data_6June2017\\dragonsRelations_FIRES-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_A_ID, ENTITY_B_ID, START_DATE, DURATION),
                5000);
        transformer.transform();
    }

}
