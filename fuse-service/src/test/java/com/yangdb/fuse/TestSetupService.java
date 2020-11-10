package com.yangdb.fuse;

import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.FuseUtils;
import com.yangdb.test.TestSetupBase;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TestSetupService extends TestSetupBase {
    protected static FuseApp app = null;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (app != null)
            app.stop();
    }

    public static void startFuse(Path path, boolean startFuse) {
        // Start fuse app (based on Jooby app web server)
        if (startFuse) {
            // Load fuse engine config file
            String confFilePath = path.toString();
            //load configuration
            Config config = FuseUtils.loadConfig(new File(confFilePath), "activeProfile");
            String[] joobyArgs = new String[]{
                    "logback.configurationFile=" + Paths.get("src", "test", "resources", "config", "logback.xml").toString(),
                    "server.join=false"
            };

            app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                    .conf(path.toFile(), "activeProfile");
            app.start("server.join=false");
        }
    }
}
