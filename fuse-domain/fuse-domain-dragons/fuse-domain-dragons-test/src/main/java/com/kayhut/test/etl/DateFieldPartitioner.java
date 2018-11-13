package com.kayhut.test.etl;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public class DateFieldPartitioner implements Partitioner {
    private String partitionField;
    private String partitionFormat;
    private SimpleDateFormat simpleDateFormat ;
    private SimpleDateFormat originalDateFormat;

    public DateFieldPartitioner(String partitionField, String partitionFormat, String originaldateFormat, String dateFormat) {
        this.partitionField = partitionField;
        this.partitionFormat = partitionFormat;

        if (originaldateFormat != null) {
            this.originalDateFormat = new SimpleDateFormat(originaldateFormat);
        }

        if (dateFormat != null) {
            this.simpleDateFormat = new SimpleDateFormat(dateFormat);
        }
    }

    @Override
    public String getPartition(Map<String, String> document) {
        String date = null;

        if (this.originalDateFormat == null) {
            date = simpleDateFormat.format(new Date(Long.parseLong(document.get(partitionField))));
        } else {
            try {
                date = simpleDateFormat.format(this.originalDateFormat.parse(document.get(partitionField)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return String.format(partitionFormat, date);
    }
}
