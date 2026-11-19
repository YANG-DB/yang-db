package com.yangdb.fuse.embedded;


// TODO - use this test to make use of open search test framework

//import com.carrotsearch.randomizedtesting.RandomizedRunner;
//import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.typesafe.config.Config;
import com.yangdb.fuse.services.FuseRunner;
import com.yangdb.fuse.services.FuseUtils;
import org.junit.Ignore;
import org.junit.Test;
//import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

//@RunWith(RandomizedRunner.class)
//@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public class EmbeddedNodeRunner {

    public static final String applicationConfFilename = "config/application.conf";
    public static final String activeProfile = "activeProfile";
    public static final String logbackConfigurationFilename = "config/logback.xml";

    // FIXME (dev-opensearch migration): disabled - this is not a real automated test, it's a
    // manual dev runner that was accidentally picked up by surefire via @Test (see the file's
    // own "TODO - use this test to make use of open search test framework" comment above, and
    // the class name "...Runner" rather than "...Test"). Currently fails fast with
    // com.typesafe.config.ConfigException$Missing: No configuration setting found for key
    // 'opensearch' (config/application.conf doesn't define it), but even if that's fixed this
    // would hang the build forever: it ends with Thread.currentThread().join(), and a running
    // thread blocking on its own join() never returns. Needs to be rewritten as a real
    // assertion-based test (or moved out of src/test entirely) before it can run in the suite.
    @Ignore("Manual dev runner mistakenly annotated as a test; hangs forever via self-join - see comment above")
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
