package com.yangdb.fuse.model.resourceInfo;

/*-
 * #%L
 * fuse-model
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

/*-
 *
 * FuseError.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by lior.perry on 6/11/2017.
 */
public class FuseError {
    private String errorCode;
    private String errorDescription;

    public FuseError() {}

    public FuseError(String errorCode, Exception e) {
        this.errorCode = errorCode;
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        //todo check is in debug mode
        e.printStackTrace();
        this.errorDescription = e.getMessage()!=null ? e.getMessage() : sw.toString();

    }

    public FuseError(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    public String toString() {
        return "FuseError{" +
                "errorCode='" + errorCode + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }


    public static class FuseErrorException extends RuntimeException {
        private final FuseError error;

        public FuseErrorException(String message, FuseError error) {
            super(message);
            this.error = error;
        }

        public FuseErrorException(FuseError error) {
            super();
            this.error = error;
        }

        public FuseErrorException(String message, Throwable cause, FuseError error) {
            super(message, cause);
            this.error = error;
        }

        public FuseError getError() {
            return error;
        }

        @Override
        public String toString() {
            return "FuseErrorException{" +
                    "error=" + error +
                    '}';
        }
    }
}
