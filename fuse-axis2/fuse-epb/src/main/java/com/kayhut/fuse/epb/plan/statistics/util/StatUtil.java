package com.kayhut.fuse.epb.plan.statistics.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Created by benishue on 24-May-17.
 */
public class StatUtil {

    private static final Logger logger = LoggerFactory.getLogger(com.kayhut.fuse.stat.util.StatUtil.class);

    public static String hashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bucketDescriptionBytes = message.getBytes("UTF8");
            byte[] bucketHash = digest.digest(bucketDescriptionBytes);

            return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(bucketHash).replaceAll("\\s", "");
        } catch (NoSuchAlgorithmException e) {

            logger.error("Could not hash the message: {}", message);
            logger.error("The hash algorithm used is not supported. Stack trace follows.", e);

            return null;
        } catch (UnsupportedEncodingException e) {

            logger.error("Could not hash the message: {}", message);
            logger.error("The character encoding used is not supported. Stack trace follows.", e);

            return null;
        }
    }
}
