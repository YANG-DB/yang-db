package com.kayhut.test.framework.index;

import org.elasticsearch.client.transport.TransportClient;

/**
 * Created by moti on 3/21/2017.
 */
public interface ElasticIndexConfigurer {
    void configure(TransportClient client);

}
