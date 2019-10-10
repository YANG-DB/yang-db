package com.yangdb.fuse.model;

/*-
 *
 * Next.java - fuse-model - yangdb - 2,016
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

/**
 * Created by lior.perry on 5/8/2017.
 */
public interface Tagged {
    String TAG_EVAL = "$:{}";

    static String tagSeq(String value) {
        return value + TAG_EVAL;
    }

    static boolean isSeq(Tagged value) {
        return value.geteTag().contains(TAG_EVAL);
    }

    static Tagged setSeq(int eNum,Tagged value) {
        value.seteTag(value.geteTag().replace("{}", Integer.toString(eNum)));
        return value;
    }

    String geteTag();

    void seteTag(String eTag);

}
