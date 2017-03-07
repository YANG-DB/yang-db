package com.kayhut.fuse.neo4jexts;

import org.neo4j.helpers.Service;
import org.neo4j.kernel.api.index.SchemaIndexProvider;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;

/**
 * Created by User on 05/03/2017.
 */
@Service.Implementation(KernelExtensionFactory.class)
public class ReddisIndexProviderFactory extends KernelExtensionFactory<ReddisIndexProviderFactory.Dependencies> {

    public static final String KEY = "reddis-index";

    public static final SchemaIndexProvider.Descriptor PROVIDER_DESCRIPTOR =
            new SchemaIndexProvider.Descriptor(KEY, "1.0");

    private final ReddisSchemaIndexProvider singleProvider;

    /**
     * Create a new instance of this kernel extension.
     *
     * @param context      the context the extension should be created for
     * @param dependencies deprecated
     * @return the {@link Lifecycle} for the extension
     * @throws Throwable if there is an error
     */
    @Override
    public Lifecycle newInstance(KernelContext context, ReddisIndexProviderFactory.Dependencies dependencies) throws Throwable {
        throw new Exception("Trying to create new instance of com.kayhut.fuse.neo4jexts.ReddisSchemaIndexProvider !!!");
    }

    public interface Dependencies {
        Config getConfig();
    }

    public ReddisIndexProviderFactory() {
        this(null);
    }

    public ReddisIndexProviderFactory(ReddisSchemaIndexProvider singleProvider) {
        super(KEY);
        this.singleProvider = singleProvider;
    }

    private boolean hasSingleProvider() {
        return singleProvider != null;
    }
}
