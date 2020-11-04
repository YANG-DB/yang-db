package com.yangdb.fuse.dispatcher.utils;

import com.yangdb.fuse.dispatcher.convertion.CsvToRdfConverter;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CsvToRdfConverterTest {
    @Test
    @Ignore
    public void csvToRdf() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("utils/input.csv");
        assert stream != null;
        String rdf = IOUtils.toString(stream, StandardCharsets.UTF_8);
        String json = CsvToRdfConverter.csvToRdf(rdf);
        Assert.assertEquals("[ {\n" +
                "  \"name\" : \"Back to the Future\",\n" +
                "  \"year\" : \"1985\",\n" +
                "  \"genres\" : \"Adventure | Comedy | Sci-Fi\",\n" +
                "  \"runtime\" : \"116 min\"\n" +
                "}, {\n" +
                "  \"name\" : \"The Godfather\",\n" +
                "  \"year\" : \"1972\",\n" +
                "  \"genres\" : \"Crime | Drama\",\n" +
                "  \"runtime\" : \"2h 55min\"\n" +
                "} ]", json);
    }

}