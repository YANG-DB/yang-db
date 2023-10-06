package com.yangdb.fuse.executor.utils;

/*-
 * #%L
 * fuse-dv-core
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

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class FileUtils {
    private static final String suffix = ".splitPart";

    /**
     * zip file
     *
     * @param source
     * @param target
     * @throws IOException
     */
    public static void zip(File source, String target) throws IOException {
        FileOutputStream fos = new FileOutputStream(target);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(source);
        ZipEntry zipEntry = new ZipEntry(source.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        fis.close();
        fos.close();
    }

    /**
     * unzip compressed file
     *
     * @param file
     * @param target
     * @throws IOException
     */
    public static void unzip(File file, String target) throws IOException {

        java.util.zip.ZipFile zipFile = new ZipFile(file);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                //todo sanitize entry name
                File entryDestination = new File(target, entry.getName());
                if (!entryDestination.toPath().normalize().startsWith(target)) {
                    throw new IOException("Bad zip entry");
                }
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        } finally {
            zipFile.close();
        }
    }

    /**
     * gunzip compressed file
     *
     * @param file
     * @param target
     * @throws IOException
     */
    public static void gunzip(File file, String target) throws IOException {

        byte[] buffer = new byte[1024];

        GZIPInputStream gzis =
                new GZIPInputStream(new FileInputStream(file));

        try {
            FileOutputStream out =
                    new FileOutputStream(target);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            gzis.close();
        }
    }

    /**
     * Split a file into multiples files.
     *
     * @param fileName   Name of file to be split.
     * @param mBperSplit maximum number of MB per file.
     * @throws IOException
     */
    public static List<Path> splitFile(final String fileName, final String target, final int mBperSplit) throws IOException {

        if (mBperSplit <= 0) {
            throw new IllegalArgumentException("mBperSplit must be more than zero");
        }

        List<Path> partFiles = new ArrayList<>();
        final long sourceSize = Files.size(Paths.get(fileName));
        final long bytesPerSplit = 1024L * 1024L * mBperSplit;
        final long numSplits = sourceSize / bytesPerSplit;
        final long remainingBytes = sourceSize % bytesPerSplit;
        int position = 0;

        try (RandomAccessFile sourceFile = new RandomAccessFile(fileName, "r");
             FileChannel sourceChannel = sourceFile.getChannel()) {

            for (; position < numSplits; position++) {
                //write multipart files.
                writePartToFile(target, bytesPerSplit, position, sourceChannel, partFiles);
            }

            if (remainingBytes > 0) {
                writePartToFile(target, remainingBytes, position, sourceChannel, partFiles);
            }
        }
        return partFiles;
    }

    private static void writePartToFile(final String target, long byteSize, long position, FileChannel sourceChannel, List<Path> partFiles) throws IOException {
        Path fileName = Paths.get(target + position + suffix);
        try (RandomAccessFile toFile = new RandomAccessFile(fileName.toFile(), "rw");
             FileChannel toChannel = toFile.getChannel()) {
            sourceChannel.position(position * byteSize);
            toChannel.transferFrom(sourceChannel, 0, byteSize);
        }
        partFiles.add(fileName);
    }
}
