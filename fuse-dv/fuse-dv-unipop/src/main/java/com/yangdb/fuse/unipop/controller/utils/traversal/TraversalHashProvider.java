package com.yangdb.fuse.unipop.controller.utils.traversal;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Created by Roman on 15/05/2017.
 */
public class TraversalHashProvider implements TraversalValueProvider<String> {
    //region Constructor
    public TraversalHashProvider(TraversalValueProvider<String> innerIdProvider, String hashAlgorithm) throws NoSuchAlgorithmException {
        this.innerIdProvider = innerIdProvider;
        this.md = MessageDigest.getInstance(hashAlgorithm);
        this.maxNumBytes = Optional.empty();
    }

    public TraversalHashProvider(TraversalValueProvider<String> innerIdProvider, String hashAlgorithm, int maxNumBytes) throws NoSuchAlgorithmException {
        this.innerIdProvider = innerIdProvider;
        this.md = MessageDigest.getInstance(hashAlgorithm);
        this.maxNumBytes = Optional.of(maxNumBytes);
    }
    //endregion

    //region TraversalIdProvider Implementation
    @Override
    public String getValue(Traversal traversal) {
        String traversalId = this.innerIdProvider.getValue(traversal);
        try {
            byte[] traversalIdBytes = traversalId.getBytes("UTF-8");
            byte[] hashBytes = md.digest(traversalIdBytes);
            return bytesToHex(hashBytes, maxNumBytes.orElseGet(() -> hashBytes.length));
        } catch(UnsupportedEncodingException ex) {
            // ???
        }

        return null;
    }
    //endregion

    //region Private Static Methods
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes, int numBytes) {
        char[] hexChars = new char[numBytes * 2];
        for ( int j = 0; j < numBytes; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    //endregion

    //region Fields
    private TraversalValueProvider<String> innerIdProvider;
    private MessageDigest md;
    private Optional<Integer> maxNumBytes;
    //endregion
}
