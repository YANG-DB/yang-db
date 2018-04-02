package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import javaslang.collection.Stream;
import org.jooby.Jooby;

import java.io.File;

/**
 * Created by Roman on 05/06/2017.
 */
public class FuseRunner {
    public static void main(final String[] args) {
        System.out.println("Args:");
        Stream.of(args).forEach(System.out::println);

        final String applicationConfFilename = args.length > 0 ?
                args[0] : "application.conf";

        final String activeProfile = args.length > 1 ?
                args[1] : "activeProfile";

        final String logbackConfigurationFilename = args.length > 2 ?
                args[2] : "logback.xml";

        new FuseRunner().run(new Options(applicationConfFilename, activeProfile, logbackConfigurationFilename, true));
    }

    public void run() {
        this.run(null, new Options());
    }

    public void run(Jooby app) {
        this.run(app, new Options());
    }

    public void run(Options options) {
        this.run(null, options);
    }

    public void run(Jooby app, Options options) {
        String[] joobyArgs = new String[]{
                "logback.configurationFile=" + options.getLogbackConfigrationFilename(),
                "server.join=" + (options.isServerJoin() ? "true" : "false")
        };

        String confFilename = options.getApplicationConfFilename() !=null ? options.getApplicationConfFilename() : "application.conf";
        File configFile = new File(confFilename);
        if (!configFile.exists()) {
            System.out.println("ConfigFile  " + confFilename + " Not Found - fallback getTo application.conf");
        }

        Jooby.run(() -> app != null ?
                app :
                new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                        .conf(configFile, options.getActiveProfile()),
                joobyArgs);
    }

    public static class Options {
        //region Constructors
        public Options() {
            this("application.conf", "activeProfile", "logback.xml", true);
        }

        public Options(String logbackConfigrationFilename, boolean serverJoin) {
            this(null, null, logbackConfigrationFilename, serverJoin);
        }

        public Options(String applicationConfFilename, String activeProfile, String logbackConfigrationFilename, boolean serverJoin) {
            this.applicationConfFilename = applicationConfFilename;
            this.activeProfile = activeProfile;
            this.logbackConfigrationFilename = logbackConfigrationFilename;
            this.serverJoin = serverJoin;
        }
        //endregion

        //region Properties
        public String getApplicationConfFilename() {
            return applicationConfFilename;
        }

        public String getActiveProfile() {
            return activeProfile;
        }

        public String getLogbackConfigrationFilename() {
            return logbackConfigrationFilename;
        }

        public boolean isServerJoin() {
            return serverJoin;
        }
        //endregion

        //region Fields
        private String applicationConfFilename;
        private String activeProfile;
        private String logbackConfigrationFilename;
        private boolean serverJoin;
        //endregion
    }
}
