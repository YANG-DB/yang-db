package org.unipop.util;

/*-
 * #%L
 * unipop-core
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

/*-
 *
 * MultiDateFormat.java - unipop-core - yangdb - 2,016
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


import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sbarzilay on 8/19/16.
 */
public class MultiDateFormat extends DateFormat {
    private List<SimpleDateFormat> formats;

    public MultiDateFormat(String format, String... formats) {
        this.formats = new ArrayList<>();
        this.formats.add(new SimpleDateFormat(format));
        for (String f : formats) {
            this.formats.add(new SimpleDateFormat(f));
        }
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return this.formats.get(0).format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Date parse(String source) throws ParseException {
        for (SimpleDateFormat format : formats) {
            try {
                return format.parse(source);
            }
            catch (ParseException ignored){}
        }
        throw new ParseException(source, 0);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }
}
