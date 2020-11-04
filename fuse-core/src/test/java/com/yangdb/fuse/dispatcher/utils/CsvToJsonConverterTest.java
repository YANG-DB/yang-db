package com.yangdb.fuse.dispatcher.utils;

import com.yangdb.fuse.dispatcher.convertion.CsvToJsonConverter;
import com.yangdb.fuse.model.ontology.EnumeratedType;
import com.yangdb.fuse.model.ontology.Value;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CsvToJsonConverterTest {
    @Test
    public void csvToJson() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("utils/input.csv");
        assert stream != null;
        String csv = IOUtils.toString(stream, StandardCharsets.UTF_8);
        String json = CsvToJsonConverter.csvToJson(csv);
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

    @Test
    public void csvToEnum() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("utils/lov_Types.csv");
        assert stream != null;
        String csv = IOUtils.toString(stream, StandardCharsets.UTF_8);
        EnumeratedType type = CsvToJsonConverter.csvToEnum("CyberObjects",0,1,csv);
        Assert.assertEquals(type.toString(),"EnumeratedType [values = [Value [val = 0, name = File], Value [val = 1, name = Process], Value [val = 2, name = RegistryValue], Value [val = 3, name = User], Value [val = 4, name = Driver], Value [val = 5, name = Socket], Value [val = 6, name = Group], Value [val = 9, name = RegistreyKey], Value [val = 10, name = NIC], Value [val = 11, name = Routing entry], Value [val = 12, name = SystemInfo], Value [val = 13, name = Session], Value [val = 14, name = Login], Value [val = 15, name = Logoff], Value [val = 16, name = Service], Value [val = 17, name = Event Log], Value [val = 18, name = Disk Drive], Value [val = 19, name = Partition], Value [val = 20, name = BIOS], Value [val = 21, name = Host], Value [val = 22, name = OS], Value [val = 23, name = Page File], Value [val = 24, name = Hot Fix], Value [val = 25, name = Physical Disk], Value [val = 26, name = Kernel Table], Value [val = 28, name = Scheduled Task], Value [val = 50027, name = URL], Value [val = 50028, name = Network Share]], eType = CyberObjects]");
    }

}