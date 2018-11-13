package com.kayhut.fuse.model.log;

/*-
 * #%L
 * Trace.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import javaslang.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by lior.perry on 5/9/2017.
 */
public interface Trace<T> extends Cloneable{
    void log(T event, Level level);

    List<Tuple2<String,T>> getLogs(Level level);

    String who();

    static Trace empty() {
        return EMPTY.instance;
    }

    static StringJoiner asString(Trace t, Level level, StringJoiner joiner) {
        t.getLogs(level).forEach(p->joiner.add(p.toString()));
        return joiner;
    }

    static <T> Trace<T> build(String who) {
        Level level = LogManager.getLogManager().getLogger("").getLevel();
        if(level.intValue() <= Level.INFO.intValue())
            return new TraceAble<>(who);
        //todo check different log levels
        return empty();
    }

    class EMPTY implements Trace{
        public static final EMPTY instance = new EMPTY();

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
        public Trace clone() {
            return this;
        }
    }
}
