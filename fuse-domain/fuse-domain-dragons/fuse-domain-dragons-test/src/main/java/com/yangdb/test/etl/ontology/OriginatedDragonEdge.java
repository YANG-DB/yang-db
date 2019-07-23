package com.yangdb.test.etl.ontology;

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

import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.test.etl.*;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.yangdb.fuse.model.execution.plan.Direction.out;
import static com.yangdb.test.scenario.ETLUtils.*;

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
                Stream.ofAll(indexPartition(DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                DRAGON,
                redundant(ORIGINATED, out,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                KINGDOM,
                out.name());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(getClient(),
                redundant(ORIGINATED,  Direction.in, "A"),
                ENTITY_A_ID,
                Stream.ofAll(indexPartition(KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                KINGDOM,
                redundant(ORIGINATED, Direction.in,"B"),
                ENTITY_B_ID,
                Stream.ofAll(indexPartition(DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                DRAGON, Direction.in.name());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ENTITY_A_ID, ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(SINCE);
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
                Arrays.asList(ID, ENTITY_A_ID, ENTITY_B_ID,  SINCE),
                5000);
        transformer.transform();
    }

}
