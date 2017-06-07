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
public interface MemberOfGuildEdge {


    static void main(String args[]) throws IOException {

        // FREEZE
        // Add sideB type
        // redundant field
        // dup + add direction

        AddConstantFieldTransformer constantFieldTransformer = new AddConstantFieldTransformer(ENTITY_B_TYPE, PERSON);
        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(getClient(),
                redundant(MEMBER_OF_GUILD,  Direction.out, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(PERSON).getIndices()).toJavaList(),
                PERSON,
                redundant(MEMBER_OF_GUILD, Direction.out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(GUILD).getIndices()).toJavaList(),
                GUILD,
                Direction.out.name());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(getClient(),
                redundant(MEMBER_OF_GUILD,  Direction.in, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(GUILD).getIndices()).toJavaList(),
                GUILD,
                redundant(MEMBER_OF_GUILD, Direction.in,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(PERSON).getIndices()).toJavaList(),
                PERSON, Direction.in.name());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE, END_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, MEMBER_OF_GUILD);
        ChainedTransformer chainedTransformer = new ChainedTransformer(constantFieldTransformer,
                duplicateEdgeTransformer,
                redundantOutTransformer,
                redundantInTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\guildsRelations_MEMBER_OF_GUILD.csv",
                "C:\\demo_data_6June2017\\guildsRelations_MEMBER_OF_GUILD-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_B_ID, ENTITY_A_ID,  START_DATE, END_DATE),
                5000);
        transformer.transform();

    }

}
