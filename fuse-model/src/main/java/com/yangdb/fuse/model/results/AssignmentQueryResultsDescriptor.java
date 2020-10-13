package com.yangdb.fuse.model.results;

import com.yangdb.fuse.model.descriptors.Descriptor;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AssignmentQueryResultsDescriptor implements Descriptor<AssignmentsQueryResult<Entity,Relationship>> {
    @Override
    public String describe(AssignmentsQueryResult<Entity,Relationship> queryResult) {
        StringJoiner joiner = new StringJoiner("\n", "", "");

        List<StringJoiner> collect = queryResult.getAssignments().stream()
                .map(AssignmentDescriptor::print)
                .map(joiner::add)
                .collect(Collectors.toList());

        return collect.stream().map(StringJoiner::toString).collect(Collectors.joining());
    }
}

