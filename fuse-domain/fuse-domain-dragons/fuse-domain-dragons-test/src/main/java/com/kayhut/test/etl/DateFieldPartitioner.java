package com.kayhut.test.etl;

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
