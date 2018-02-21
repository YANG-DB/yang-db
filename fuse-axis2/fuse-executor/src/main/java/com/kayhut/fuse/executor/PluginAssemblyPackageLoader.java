package com.kayhut.fuse.executor;

import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.OntologyGraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.schema.InitialGraphDataLoader;
import com.kayhut.fuse.executor.ontology.schema.RawElasticSchema;
import com.typesafe.config.Config;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by lior.perry on 2/18/2018.
 */
public class PluginAssemblyPackageLoader extends ExecutorModule {

    protected GraphElementSchemaProviderFactory createSchemaProviderFactory(Config conf) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        RawElasticSchema rawElasticSchema =
                (RawElasticSchema) (Class.forName(
                        conf.getString(conf.getString("assembly")+".physical_raw_schema")).newInstance());

        GraphElementSchemaProviderFactory physicalSchemaProviderFactory =
                (GraphElementSchemaProviderFactory) (Class.forName(
                        conf.getString(conf.getString("assembly")+".physical_schema_provider_factory_class"))
                        .getConstructor(Config.class, RawElasticSchema.class)
                        .newInstance(conf,rawElasticSchema));

        return new OntologyGraphElementSchemaProviderFactory(physicalSchemaProviderFactory);
    }

    protected InitialGraphDataLoader createInitialDataLoader(Config conf) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        RawElasticSchema rawElasticSchema =
                (RawElasticSchema) (Class.forName(
                        conf.getString(conf.getString("assembly")+".physical_raw_schema")).newInstance());

        InitialGraphDataLoader initialGraphDataLoader =
                (InitialGraphDataLoader) (Class.forName(
                        conf.getString(conf.getString("assembly")+".physical_schema_data_loader"))
                        .getConstructor(Config.class, RawElasticSchema.class)
                        .newInstance(conf,rawElasticSchema));

        return initialGraphDataLoader;
    }


}
