package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

class MetadataFactory {
    //region Constructors

    MetadataFactory() {
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    //endregion

    public LogicalElementBase.Metadata createMetadata(Vertex vertex) {
        ArrayList<String> authorizations = vertex.value(PhysicalElementProperties.AUTHORIZATION);
        // TODO: auth
//        ArrayList<LogicalItemBase.Metadata.Authorization> logicalAuts = authorizations.stream().map(auth ->
//                new LogicalItemBase.Metadata.Authorization(auth)).collect(Collectors.toList());

        try {
            return new LogicalElementBase.Metadata(
                    vertex.value(PhysicalElementProperties.CREATION_USER),
                    this.dateFormatter.parse(vertex.value(PhysicalElementProperties.CREATION_TIME)),
                    vertex.value(PhysicalElementProperties.LAST_UPDATED_USER),
                    this.dateFormatter.parse(vertex.value(PhysicalElementProperties.LAST_UPDATED_TIME)),
                    new ArrayList<>()
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    //endregion

    //region Private
    private DateFormat dateFormatter;
}
