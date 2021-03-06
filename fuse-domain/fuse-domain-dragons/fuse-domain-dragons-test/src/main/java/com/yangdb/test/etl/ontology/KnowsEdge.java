package com.yangdb.test.etl.ontology;

/*-
 * #%L
 * fuse-domain-dragons-test
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.test.etl.*;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.yangdb.fuse.model.execution.plan.Direction.both;
import static com.yangdb.test.scenario.ETLUtils.*;

/**
 * Created by moti on 6/7/2017.
 */
public interface KnowsEdge {
    static void main(String args[]) throws IOException {

        // Fires
        // Add sideB type
        // redundant field
        // dup + add direction

        Map<String, String> constFields=  new HashMap<>();
        constFields.put(ENTITY_A_TYPE, PERSON);
        constFields.put(ENTITY_B_TYPE, PERSON);
        AddConstantFieldsTransformer constantFieldsTransformer = new AddConstantFieldsTransformer(constFields, both);
        RedundantFieldTransformer redundantFieldTransformer = new RedundantFieldTransformer(getClient(),
                redundant(KNOWS, Direction.out,"A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                PERSON,
                redundant(KNOWS, Direction.out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                PERSON);
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, KNOWS);
        ChainedTransformer chainedTransformer = new ChainedTransformer(constantFieldsTransformer,
                duplicateEdgeTransformer,
                redundantFieldTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\personsRelations_KNOWS.csv",
                "C:\\demo_data_6June2017\\personsRelations_KNOWS-out.csv",
                chainedTransformer,
                Arrays.asList(ID, ENTITY_A_ID, ENTITY_B_ID, START_DATE),
                5000);
        transformer.transform();
    }

}
