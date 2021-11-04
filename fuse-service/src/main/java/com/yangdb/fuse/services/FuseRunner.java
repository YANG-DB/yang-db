package com.yangdb.fuse.services;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.jooby.Jooby;

import java.io.File;

/**
 * Created by Roman on 05/06/2017.
 */
public class FuseRunner {
    public static void main(final String[] args) throws Exception {
        System.out.println("Args:");
        Stream.of(args).forEach(System.out::println);

        final String applicationConfFilename = args.length > 0 ?
                args[0] : "application.conf";

        final String activeProfile = args.length > 1 ?
                args[1] : "activeProfile";

        final String logbackConfigurationFilename = args.length > 2 ?
                args[2] : "logback.xml";

        final String opensearchOnly = args.length > 3 ?
                args[3] : "false";

        new FuseRunner().run(new Options(applicationConfFilename, activeProfile, logbackConfigurationFilename, true, Boolean.parseBoolean(opensearchOnly)));
    }

    public void run() throws Exception {
        this.run(null, new Options());
    }

    public void run(Jooby app) throws Exception {
        this.run(app, new Options());
    }

    public void run(Options options) throws Exception {
        this.run(null, options);
    }

    public void run(Jooby app, Options options) throws Exception {
        String[] joobyArgs = new String[]{
                "logback.configurationFile=" + options.getLogbackConfigrationFilename(),
                "server.join=" + (options.isServerJoin() ? "true" : "false")
        };

        String confFilename = options.getApplicationConfFilename() != null ? options.getApplicationConfFilename() : "application.conf";
        File configFile = new File(confFilename);
        if (!configFile.exists()) {
            System.out.println("ConfigFile  " + confFilename + " Not Found - fallback getTo application.conf");
        }

        //load configuration
        Config config = FuseUtils.loadConfig(configFile, options.getActiveProfile());
        //try load embedded if required
        FuseUtils.loadEmbedded(config);

        if (options.isOpensearchOnly()) {
            Thread.currentThread().join();
            //break when only opensearch is required
        } else {
            //load jooby App
            Jooby.run(() -> app != null ?
                            app :
                            new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                                    .conf(config)
                                    .throwBootstrapException(),
                    joobyArgs);
        }
    }

    public static class Options {
        //region Constructors
        public Options() {
            this("application.conf", "activeProfile", "logback.xml", true, false);
        }

        public Options(String logbackConfigrationFilename, boolean serverJoin, boolean opensearchOnly) {
            this(null, null, logbackConfigrationFilename, serverJoin, opensearchOnly);
        }

        public Options(String applicationConfFilename, String activeProfile, String logbackConfigrationFilename, boolean serverJoin, boolean opensearchOnly) {
            this.applicationConfFilename = applicationConfFilename;
            this.activeProfile = activeProfile;
            this.logbackConfigrationFilename = logbackConfigrationFilename;
            this.serverJoin = serverJoin;
            this.opensearchOnly = opensearchOnly;
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

        public boolean isOpensearchOnly() {
            return opensearchOnly;
        }

        //endregion

        //region Fields
        private String applicationConfFilename;
        private String activeProfile;
        private String logbackConfigrationFilename;
        private boolean serverJoin;
        private boolean opensearchOnly;
        //endregion
    }
}
