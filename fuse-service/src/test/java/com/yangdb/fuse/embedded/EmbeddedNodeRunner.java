package com.yangdb.fuse.embedded;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.typesafe.config.Config;
import com.yangdb.fuse.services.FuseRunner;
import com.yangdb.fuse.services.FuseUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

@RunWith(RandomizedRunner.class)
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public class EmbeddedNodeRunner {

    public static final String applicationConfFilename = "config/application.conf";
    public static final String activeProfile = "activeProfile";
    public static final String logbackConfigurationFilename = "config/logback.xml";

    @Test
    public void runEmbeddedNodeTest() throws Exception {
        final FuseRunner.Options options = new FuseRunner.Options(applicationConfFilename, activeProfile, logbackConfigurationFilename, true, true);
        String confFilename = options.getApplicationConfFilename() != null ? options.getApplicationConfFilename() : "application.conf";
        File configFile = new File(confFilename);
        if (!configFile.exists()) {
            System.out.println("ConfigFile  " + confFilename + " Not Found - fallback getTo application.conf");
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(applicationConfFilename);
            configFile = new File(resource.getFile());
        }
        Config config = FuseUtils.loadConfig(configFile, options.getActiveProfile());
        final boolean embedded = FuseUtils.loadEmbedded(config);
        Thread.currentThread().join();
    }
}
