package com.kayhut.fuse.model.results;

/*-
 * #%L
 * CsvQueryResult.java - fuse-model - kayhut - 2,016
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
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

public class CsvQueryResult extends QueryResultBase implements TextContent{

    public CsvQueryResult() {
    }

    public CsvQueryResult(String[] csvLines) {
        this.csvLines = csvLines;
    }

    public CsvQueryResult(String[] csvLines, String[] headers) {
        this.csvLines = csvLines;
        this.headers = headers;
    }

    public void setCsvLines(String[] csvLines) {
        this.csvLines = csvLines;
    }

    public String[] getCsvLines() {
        return csvLines;
    }

    public String getResultType(){
        return "csv";
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    @Override
    public int getSize() {
        return this.csvLines.length;
    }

    private String[] csvLines;
    private String[] headers;

    @Override
    public String content() {
        StringBuilder builder = new StringBuilder();
        if(this.headers != null) {
            builder.append(String.join(",", headers));
            builder.append('\n');
        }
        for (int i = 0; i < csvLines.length; i++) {
            builder.append(csvLines[i]);
            if(i != csvLines.length-1){
                builder.append('\n');
            }
        }
        return builder.toString();
    }


    public static final class Builder {
        public Builder() {
            this.lines = new ArrayList<>();
        }
        public static Builder instance() {
            return new Builder();
        }
        public Builder withLine(String line){
            this.lines.add(line);
            return this;
        }

        public Builder withHeader(String[] header){
            this.headers = header;
            return this;
        }

        public CsvQueryResult build(){
            if(this.headers != null) {
                return new CsvQueryResult(Stream.ofAll(this.lines).toJavaArray(String.class), this.headers);
            }else{
                return new CsvQueryResult(Stream.ofAll(this.lines).toJavaArray(String.class));
            }
        }

        private List<String> lines;
        private String[] headers;
    }
}
