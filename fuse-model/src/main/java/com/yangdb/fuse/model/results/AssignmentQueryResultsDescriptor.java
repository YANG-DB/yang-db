package com.yangdb.fuse.model.results;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
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

import com.yangdb.fuse.model.descriptors.Descriptor;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AssignmentQueryResultsDescriptor implements Descriptor<AssignmentsQueryResult<Entity,Relationship>> {
    private static AssignmentQueryResultsDescriptor INSTANCE = new AssignmentQueryResultsDescriptor();

    @Override
    public String describe(AssignmentsQueryResult<Entity,Relationship> queryResult) {
        StringJoiner joiner = new StringJoiner("\n", "", "");

        List<StringJoiner> collect = queryResult.getAssignments().stream()
                .map(AssignmentDescriptor::print)
                .map(joiner::add)
                .collect(Collectors.toList());

        return collect.stream().map(StringJoiner::toString).collect(Collectors.joining());
    }

    public static String print(AssignmentsQueryResult<Entity,Relationship> queryResult) {
        return INSTANCE.describe(queryResult);
    }


}
