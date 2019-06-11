package org.unipop.schema.property;

/*-
 * #%L
 * DatePropertySchema.java - unipop-core - kayhut - 2,016
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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by sbarzilay on 8/2/16.
 */
public interface DatePropertySchema {
    DateFormat getSourceDateFormat();

    DateFormat getDisplayDateFormat();

    default Date fromSource(String date) {
        try {
            return getSourceDateFormat().parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("couldn't parse date:{0} using:{1}", date, getSourceDateFormat()));
        }
    }

    default Date fromDisplay(String date) {
        try {
            return getDisplayDateFormat().parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("couldn't parse date:{0} using:{1}", date, getDisplayDateFormat()));
        }
    }

    default String toDisplay(Date date) {
        return getDisplayDateFormat().format(date);
    }

    default String toSource(Date date) {
        return getSourceDateFormat().format(date);
    }
}
