package com.kayhut.test.etl.ontology;

/*-
 * #%L
 * fuse-domain-dragons-test
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.test.etl.*;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.model.execution.plan.Direction.both;
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
        Map<String, String> constFields=  new HashMap<>();
        constFields.put(ENTITY_A_TYPE, DRAGON);
        constFields.put(ENTITY_B_TYPE, DRAGON);
        AddConstantFieldsTransformer constantFieldsTransformer = new AddConstantFieldsTransformer(constFields, both);
        RedundantFieldTransformer redundantFieldTransformer = new RedundantFieldTransformer(getClient(),
                redundant(FREEZE, Direction.out,"A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                DRAGON,
                redundant(FREEZE, Direction.out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                DRAGON);
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(START_DATE, END_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ID, DIRECTION_FIELD, FREEZE);
        ChainedTransformer chainedTransformer = new ChainedTransformer(constantFieldsTransformer,
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
