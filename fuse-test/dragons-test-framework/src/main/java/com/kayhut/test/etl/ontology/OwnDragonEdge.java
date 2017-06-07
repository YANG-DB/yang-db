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
public interface OwnDragonEdge {
    static void main(String args[]) throws IOException {
        Map<String, String> outConstFields=  new HashMap<>();
        outConstFields.put(ENTITY_A_TYPE, PERSON);
        outConstFields.put(ENTITY_B_TYPE, DRAGON);
        AddConstantFieldsTransformer outFieldsTransformer = new AddConstantFieldsTransformer(outConstFields, out);
        Map<String, String> inConstFields=  new HashMap<>();
        inConstFields.put(ENTITY_A_TYPE, DRAGON);
        inConstFields.put(ENTITY_B_TYPE, PERSON);
        AddConstantFieldsTransformer inFieldsTransformer = new AddConstantFieldsTransformer(inConstFields, Direction.in);

        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(getClient(),
                redundant(OWN, out, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(PERSON).getIndices()).toJavaList(),
                PERSON,
                redundant(OWN, out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON,
                out.name());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(getClient(),
                redundant(OWN,  Direction.in, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(DRAGON).getIndices()).toJavaList(),
                DRAGON,
                redundant(OWN, Direction.in,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(PERSON).getIndices()).toJavaList(),
                PERSON, Direction.in.name());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE, END_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, OWN + "D");
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                duplicateEdgeTransformer,
                outFieldsTransformer,
                inFieldsTransformer,
                redundantInTransformer,
                redundantOutTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\personsRelations_OWNS_DRAGON.csv",
                "C:\\demo_data_6June2017\\personsRelations_OWNS_DRAGON-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_B_ID, ENTITY_A_ID,  START_DATE, END_DATE),
                5000);
        transformer.transform();
    }

}
