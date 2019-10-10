package com.yangdb.fuse.model.log;

/*-
 *
 * TraceAble.java - fuse-model - yangdb - 2,016
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

import javaslang.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 5/9/2017.
 */
public class TraceAble<T> implements Trace<T>{
    private List<Tuple2<Level,T>> log;
    private String who;

    public TraceAble(String who) {
        this.who = who;
        log = new ArrayList<>();
    }

    @Override
    public void log(T event, Level level) {
        log.add(new Tuple2<>(level,event));
    }

    @Override
    public List<Tuple2<String,T>> getLogs(Level level) {
        return log.stream().filter(log->log._1.intValue()<=level.intValue()).map(v->new Tuple2<>(who,v._2)).collect(Collectors.toList());
    }

    @Override
    public String who() {
        return who;
    }

    @Override
    public Trace<T> clone() {
        TraceAble<T> newTrace = new TraceAble<>(who);
        newTrace.log = new ArrayList<>(log);
        return newTrace;
    }
}
