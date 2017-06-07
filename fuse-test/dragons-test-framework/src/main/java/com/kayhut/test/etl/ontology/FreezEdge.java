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
public interface FreezEdge {
    static void main(String args[]) throws IOException {

        // FREEZE
        // Add sideB type
        // redundant field
        // dup + add direction
        AddConstantFieldTransformer entityBFieldTransformer = new AddConstantFieldTransformer(ENTITY_B_TYPE, DRAGON);
        AddConstantFieldTransformer entityAFieldTransformer = new AddConstantFieldTransformer(ENTITY_A_TYPE, DRAGON);
        RedundantFieldTransformer redundantFieldTransformer = new RedundantFieldTransformer(getClient(),
                redundant(FREEZE, Direction.out,"A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON,
                redundant(FREEZE, Direction.out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON);
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE, END_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, FREEZE);
        ChainedTransformer chainedTransformer = new ChainedTransformer(entityAFieldTransformer,
                entityBFieldTransformer,
                duplicateEdgeTransformer,
                redundantFieldTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\dragonsRelations_FREEZES.csv",
                "C:\\demo_data_6June2017\\dragonsRelations_FREEZES-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_A_ID, ENTITY_B_ID, START_DATE, END_DATE),
                5000);
        transformer.transform();
    }

}
