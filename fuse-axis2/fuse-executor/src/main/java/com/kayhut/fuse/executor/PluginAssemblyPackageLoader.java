package com.kayhut.fuse.executor;

import com.kayhut.fuse.executor.ontology.schema.InitialGraphDataLoader;
import com.kayhut.fuse.executor.ontology.schema.RawElasticSchema;
import com.typesafe.config.Config;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by lior.perry on 2/18/2018.
 */
public class PluginAssemblyPackageLoader extends ExecutorModule {

    protected Class<? extends RawElasticSchema> getRawElasticSchema(Config conf) throws ClassNotFoundException {
        return (Class<? extends RawElasticSchema>) Class.forName(conf.getString(conf.getString("assembly")+".physical_raw_schema"));
    }

    protected InitialGraphDataLoader getInitialDataLoader(Config conf) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        RawElasticSchema rawElasticSchema = getRawElasticSchema(conf).newInstance();
        InitialGraphDataLoader initialGraphDataLoader =
                (InitialGraphDataLoader) (Class.forName(
                        conf.getString(conf.getString("assembly")+".physical_schema_data_loader"))
                        .getConstructor(Config.class, RawElasticSchema.class)
                        .newInstance(conf,rawElasticSchema));

        return initialGraphDataLoader;
    }


}
