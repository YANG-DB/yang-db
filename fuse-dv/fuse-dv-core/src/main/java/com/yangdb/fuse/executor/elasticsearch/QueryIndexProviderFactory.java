package com.yangdb.fuse.executor.elasticsearch;

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import org.elasticsearch.client.Client;

import java.util.Optional;

/**
 * Index Provider factory will generate an Index Provider instance specific for a query
 * Based on the ontology
 */
public class QueryIndexProviderFactory {

    private final Client client;
    private final RawSchema schema;
    private final OntologyProvider ontologyProvider;

    @Inject
    public QueryIndexProviderFactory(Client client, RawSchema schema, OntologyProvider ontologyProvider) {
        this.client = client;
        this.schema = schema;
        this.ontologyProvider = ontologyProvider;
    }

    /**
     * generate an Index Provider based on the :
     *  * Ontology -
     *  * Query -
     *  * MappingFlavor -
     *
     * @param query
     * @param flavor
     * @return
     */
    public IndexProvider generate(Query query,MappingEntitiesFlavor flavor) {
        String ont = query.getOnt();
        Optional<Ontology> ontology = ontologyProvider.get(ont);
        if(!ontology.isPresent())
            throw new FuseError.FuseErrorException(new FuseError(String.format("No Ontology named %s found",ont),String.format("No Ontology named %s found, context: %s ",ont,this.getClass().getSimpleName())));


        return new IndexProvider();
    }
}
