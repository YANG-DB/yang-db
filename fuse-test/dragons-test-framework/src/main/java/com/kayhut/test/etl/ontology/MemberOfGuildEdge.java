package com.kayhut.test.etl.ontology;

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
public interface MemberOfGuildEdge {


    static void main(String args[]) throws IOException {

        // FREEZE
        // Add sideB type
        // redundant field
        // dup + add direction
        Map<String, String> outConstFields=  new HashMap<>();
        outConstFields.put(ENTITY_A_TYPE, PERSON);
        outConstFields.put(ENTITY_B_TYPE, GUILD);
        AddConstantFieldsTransformer outFieldsTransformer = new AddConstantFieldsTransformer(outConstFields, R);
        Map<String, String> inConstFields=  new HashMap<>();
        inConstFields.put(ENTITY_A_TYPE, GUILD);
        inConstFields.put(ENTITY_B_TYPE, PERSON);
        AddConstantFieldsTransformer inFieldsTransformer = new AddConstantFieldsTransformer(inConstFields, L);

        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(getClient(),
                redundant(MEMBER_OF_GUILD, R, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                PERSON,
                redundant(MEMBER_OF_GUILD, R,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(GUILD).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                GUILD,
                R.translatedName());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(getClient(),
                redundant(MEMBER_OF_GUILD,  L, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(GUILD).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                GUILD,
                redundant(MEMBER_OF_GUILD, L,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                PERSON, L.translatedName());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE, END_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, MEMBER_OF_GUILD);
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                duplicateEdgeTransformer,
                outFieldsTransformer,
                inFieldsTransformer,
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
