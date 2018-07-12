package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

import com.kayhut.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalElementProperties;
import com.kayhut.fuse.assembly.knowledge.logical.model.*;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

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

    public ElementBaseLogical.Metadata createMetadata(Vertex vertex) {
        ArrayList<String> authorizations = vertex.value(PhysicalElementProperties.AUTHORIZATION);
        // TODO: auth
//        ArrayList<LogicalItemBase.Metadata.Authorization> logicalAuts = authorizations.stream().map(auth ->
//                new LogicalItemBase.Metadata.Authorization(auth)).collect(Collectors.toList());

        VertexProperty<String> creationUserProperty = vertex.property(PhysicalElementProperties.CREATION_USER);
        String creationUser = (creationUserProperty == VertexProperty.<String>empty()) ? null : creationUserProperty.value();

        VertexProperty<String> creationTimeProperty = vertex.property(PhysicalElementProperties.CREATION_TIME);
        String creationTime = (creationTimeProperty == VertexProperty.<String>empty()) ? null : creationTimeProperty.value();

        VertexProperty<String> lastUpdateUserProperty = vertex.property(PhysicalElementProperties.LAST_UPDATED_USER);
        String lastUpdateUser = (lastUpdateUserProperty == VertexProperty.<String>empty()) ? null : lastUpdateUserProperty.value();

        VertexProperty<String> lastUpdateTimeProperty = vertex.property(PhysicalElementProperties.LAST_UPDATED_TIME);
        String lastUpdateTime = (lastUpdateTimeProperty == VertexProperty.<String>empty()) ? null : lastUpdateTimeProperty.value();

        if (creationUser != null) {
            try {
                return new ElementBaseLogical.Metadata(
                        creationUser,
                        this.dateFormatter.parse(creationTime),
                        lastUpdateUser,
                        this.dateFormatter.parse(lastUpdateTime),
                        new ArrayList<>()
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    //endregion

    //region Private
    private DateFormat dateFormatter;
}
