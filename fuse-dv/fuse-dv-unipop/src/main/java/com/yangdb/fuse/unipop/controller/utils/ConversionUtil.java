package com.yangdb.fuse.unipop.controller.utils;

/*-
 *
 * fuse-dv-unipop
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

import javaslang.collection.Stream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Roman on 17/05/2017.
 */
public class ConversionUtil {
    public static SimpleDateFormat sdf;
    static  {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public static <S, T> T prepareValue(S value) {
        if (value instanceof Date) {
            return (T)sdf.format((Date)value);
        } else if (Iterable.class.isAssignableFrom(value.getClass())) {
            return (T)Stream.ofAll(CollectionUtil.listFromObjectValue(value))
                    .map(ConversionUtil::prepareValue)
                    .toJavaList();
        }

        return (T)value;
    }
}
