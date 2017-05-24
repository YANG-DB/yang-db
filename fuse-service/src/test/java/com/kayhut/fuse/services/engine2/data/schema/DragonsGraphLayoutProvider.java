package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphLayoutProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Roman on 23/05/2017.
 */
public class DragonsGraphLayoutProvider implements GraphLayoutProvider {
    //region Constructors
    public DragonsGraphLayoutProvider() {
        this.redundantProperties = new HashMap<>();
        Map<String, String> fireRedundantProperties = new HashMap<>();
        fireRedundantProperties.put("id", "entityB.id");
        fireRedundantProperties.put("type", "entityB.type");

        this.redundantProperties.put("Fire", fireRedundantProperties);
    }
    //endregion

    //region GraphLayoutProvider Implementation
    @Override
    public Optional<GraphRedundantPropertySchema> getRedundantProperty(String edgeType, GraphElementPropertySchema property) {
        Map<String, String> edgeRedundnatProperties = redundantProperties.get(edgeType);
        if (edgeRedundnatProperties == null) {
            return Optional.empty();
        }

        String redundantPropertyName = edgeRedundnatProperties.get(property.getName());
        if (redundantPropertyName == null) {
            return Optional.empty();
        }

        return Optional.of(new GraphRedundantPropertySchema.Impl(
                property.getName(),
                redundantPropertyName,
                property.getType()));
    }
    //endregion

    //region Fields
    private Map<String, Map<String, String>> redundantProperties;
    //endregion
}
