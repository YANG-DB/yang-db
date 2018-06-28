package com.kayhut.fuse.test.framework;

import java.io.File;

/**
 * Created by moti on 3/27/2017.
 */
public class TestUtil {
    public static void deleteFolder(String folder) {
        File folderFile = new File(folder);
        File[] files = folderFile.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        folderFile.delete();
    }
}
