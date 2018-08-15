package com.kayhut.fuse.dispatcher.modules;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.driver.*;
import com.kayhut.fuse.dispatcher.ontology.DirectoryOntologyProvider;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.ontology.SimpleOntologyProvider;
import com.kayhut.fuse.dispatcher.resource.store.InMemoryResourceStore;
import com.kayhut.fuse.dispatcher.resource.store.LoggingResourceStore;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.jooby.Env;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class NewDispatcherModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AppUrlSupplier.class).toInstance(getAppUrlSupplier(conf));
        binder.bind(OntologyProvider.class).toInstance(getOntologyProvider(conf));
    }

    //region Private Methods
    private AppUrlSupplier getAppUrlSupplier(Config conf) throws UnknownHostException {
        int applicationPort = conf.getInt("application.port");
        String baseUrl = String.format("http://%s:%d/fuse", InetAddress.getLocalHost().getHostAddress(), applicationPort);
        if (conf.hasPath("appUrlSupplier.public.baseUri")) {
            baseUrl = conf.getString("appUrlSupplier.public.baseUri");
        }

        return new DefaultAppUrlSupplier(baseUrl);
    }

    private OntologyProvider getOntologyProvider(Config conf) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return new DirectoryOntologyProvider(conf.getString("fuse.ontology_provider_dir"));
        } catch (ConfigException e) {
            return (OntologyProvider) Class.forName(conf.getString("fuse.ontology_provider")).getConstructor().newInstance();
        }
        //no ontology provider was found
    }
    //endregion
}
