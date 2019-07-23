package com.yangdb.fuse.epb.plan.statistics.util;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by benishue on 24-May-17.
 */
public class StatUtil {

    private static final Logger logger = LoggerFactory.getLogger(com.yangdb.fuse.stat.util.StatUtil.class);

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
