package com.kayhut.fuse.unipop.schemaProviders;


import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;

import java.util.Optional;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphElementSchema {
    Class getSchemaElementType();

    /**
     * @return vertex/edge type (e.g., Dragon, Person, fire)
     */
    String getType();

    Optional<GraphElementRouting> getRouting();

    IndexPartition getIndexPartition();

    Iterable<GraphElementPropertySchema> getProperties();

    Optional<GraphElementPropertySchema> getProperty(String name);

}
