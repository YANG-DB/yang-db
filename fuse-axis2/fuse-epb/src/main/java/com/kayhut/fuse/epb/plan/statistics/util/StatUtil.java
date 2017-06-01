package com.kayhut.fuse.epb.plan.statistics.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by benishue on 24-May-17.
 */
public class StatUtil {

    public static String hashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bucketDescriptionBytes = message.getBytes("UTF8");
            byte[] bucketHash = digest.digest(bucketDescriptionBytes);

            return org.elasticsearch.common.Base64.encodeBytes(bucketHash, org.elasticsearch.common.Base64.URL_SAFE).replaceAll("\\s", "");

        } catch (NoSuchAlgorithmException e) {

//            logger.error("Could not hash the message: {}", message);
//            logger.error("The hash algorithm used is not supported. Stack trace follows.", e);

            return null;
        } catch (UnsupportedEncodingException e) {

//            logger.error("Could not hash the message: {}", message);
//            logger.error("The character encoding used is not supported. Stack trace follows.", e);

            return null;
        } catch (IOException e) {

//            logger.error("Could not hash the message: {}", message);
//            logger.error("A problem occured when encoding as URL safe hash. Stack trace follows.", e);

            return null;
        }
    }
}
