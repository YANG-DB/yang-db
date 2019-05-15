package com.kayhut.fuse.executor.utils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

@Ignore
public class FileUtilsTest {

    @Test
    public void unzip() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("./soc-LiveJournal1.txt.zip");
        FileUtils.gunzip(new File(resource.getFile()),System.getProperty("user.dir")+"/target/unZipped.txt");
        final File file = new File(System.getProperty("user.dir") + "/target/unZipped.txt");
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.getTotalSpace()>0);
    }

    @Test
    public void splitFile() throws IOException {
        unzip();
        final File file = new File(System.getProperty("user.dir") + "/target/unZipped.txt");
        final List<Path> paths = FileUtils.splitFile(file.getAbsolutePath(),System.getProperty("user.dir") + "/target/target", 50);
        Assert.assertFalse(paths.isEmpty());
        Assert.assertEquals(paths.size(),21);
    }
}