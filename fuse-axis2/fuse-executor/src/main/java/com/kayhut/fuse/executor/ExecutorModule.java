package com.kayhut.fuse.executor;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorFactory;
import com.typesafe.config.Config;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jooby.Env;
import org.jooby.Jooby;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by lior on 22/02/2017.
 */
public class ExecutorModule implements Jooby.Module  {
    //region Jooby.Module Implementation
    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PageCreationOperationContext.Processor.class).to(PageProcessor.class).asEagerSingleton();
        binder.bind(CursorFactory.class).to(TraversalCursorFactory.class).asEagerSingleton();

        binder.bind(Client.class)
                .toInstance(
                        createClient(
                                conf.getStringList("elasticsearch.hosts"),
                                conf.getInt("elasticsearch.port"),
                                conf.getString("elasticsearch.clusterName")));
    }
    //endregion

    //region Private Methods
    private Client createClient(Iterable<String> hosts, int port, String clusterName) {
        Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).build();
        TransportClient client = TransportClient.builder().settings(settings).build();
        hosts.forEach(host -> {
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return client;
    }
    //endregion
}
