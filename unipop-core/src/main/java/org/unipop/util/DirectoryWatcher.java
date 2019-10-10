package org.unipop.util;

/*-
 *
 * DirectoryWatcher.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.apache.commons.configuration.Configuration;

import java.io.IOException;
import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by sbarzilay on 8/14/16.
 */
public class DirectoryWatcher {
    private WatchService service;
    private OnFileChange onFileChange;
    private Timer timer;
    private TimerTask timerTask;
    private int interval;

    public DirectoryWatcher(Path path, int interval, OnFileChange onFileChange) {
        watchDirectoryPath(path);
        this.onFileChange = onFileChange;
        timer = new Timer();
        this.interval = interval;
    }

    public void start() {
        timer.scheduleAtFixedRate(timerTask, 0,
                interval);
    }

    public void stop() throws IOException {
        timer.cancel();
        if (service != null) service.close();
    }

    private void watchDirectoryPath(Path path) {
        // Sanity check - Check if path is a folder
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path
                        + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();
        // We create the new WatchService using the new try() block
        try {
            service = fs.newWatchService();
            // We register the path to the service
            // We watch for creation events
            path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            // Start the infinite polling loop
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    WatchKey key = null;
                    try {
                        key = service.take();

                        // Dequeueing events
                        WatchEvent.Kind<?> kind = null;
                        for (WatchEvent<?> watchEvent : key.pollEvents()) {
                            // Get the type of the event
                            kind = watchEvent.kind();
                            if (OVERFLOW == kind) {
                                continue; // loop
                            } else if (ENTRY_CREATE == kind || ENTRY_MODIFY == kind || ENTRY_DELETE == kind) {
                                // A new Path was created
                                Path newPath = ((WatchEvent<Path>) watchEvent)
                                        .context();
                                if (notSwap(newPath))
                                    onFileChange.onFileChange(newPath);
                            }
                        }
                    }
                    catch (Exception ignored){
                    }
                }

            };

        } catch (IOException ignored) {
        }
    }

    private static boolean notSwap(Path file) {
        return !(file.toString().endsWith(".swp") ||
                file.toString().endsWith(".swpx") ||
                file.toString().endsWith("~"));
    }

    @FunctionalInterface
    public interface OnFileChange {
        void onFileChange(Path path) throws IOException;
    }
}
