package com.yangdb.fuse.model.log;

/*-
 * #%L
 * TraceComposite.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by lior.perry on 5/11/2017.
 */
public interface TraceComposite<T> extends Trace<T> {
    void with(Trace<T> trace);

    static <T> TraceComposite<T> build(String who) {
        Level level = LogManager.getLogManager().getLogger("").getLevel();
        if (level.intValue() <= Level.INFO.intValue())
            return new TraceCompositeAble<>(who);
        //todo check different log levels
        return empty();
    }


    static TraceComposite empty() {
        return EMPTY1.instance;
    }

    class EMPTY1 implements TraceComposite{

        public static final EMPTY1 instance = new EMPTY1();

        @Override
        public void log(Object event, Level level) {}

        @Override
        public List getLogs(Level level) {
            return Collections.emptyList();
        }

        @Override
        public String who() {
            return "EMPTY";
        }

        @Override
        public void with(Trace trace) {
        }

        @Override
        public Trace<String> clone() {
            return this;
        }

    }
}
