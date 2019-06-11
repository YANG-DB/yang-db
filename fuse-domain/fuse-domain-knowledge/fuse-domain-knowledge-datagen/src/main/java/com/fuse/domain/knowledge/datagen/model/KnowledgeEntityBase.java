package com.fuse.domain.knowledge.datagen.model;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class KnowledgeEntityBase {
    public static class Metadata{
        //region Constructors
        public Metadata() {}

        public Metadata(String creationUser, Date creationTime, String lastUpdateUser, Date lastUpdateTime) {
            this(creationUser, creationTime, lastUpdateUser, lastUpdateTime, null, null, null, null);
        }

        public Metadata(String creationUser, Date creationTime, String lastUpdateUser, Date lastUpdateTime, Iterable<String> refs) {
            this(creationUser, creationTime, lastUpdateUser, lastUpdateTime, null, null, refs, null);
        }

        public Metadata(String creationUser, Date creationTime, String lastUpdateUser, Date lastUpdateTime, Iterable<String> refs, Iterable<String> authorization) {
            this(creationUser, creationTime, lastUpdateUser, lastUpdateTime, null, null, refs, authorization);
        }

        public Metadata(
                String creationUser,
                Date creationTime,
                String lastUpdateUser,
                Date lastUpdateTime,
                String deleteUser,
                Date deleteTime) {
            this(creationUser, creationTime, lastUpdateUser, lastUpdateTime, deleteUser, deleteTime, null, null);
        }

        public Metadata(
                String creationUser,
                Date creationTime,
                String lastUpdateUser,
                Date lastUpdateTime,
                String deleteUser,
                Date deleteTime,
                Iterable<String> refs) {
            this(creationUser, creationTime, lastUpdateUser, lastUpdateTime, deleteUser, deleteTime, refs, null);
        }

        public Metadata(
                String creationUser,
                Date creationTime,
                String lastUpdateUser,
                Date lastUpdateTime,
                String deleteUser,
                Date deleteTime,
                Iterable<String> refs,
                Iterable<String> authorization) {
            this.creationUser = creationUser;
            this.creationTime = creationTime;
            this.lastUpdateUser = lastUpdateUser;
            this.lastUpdateTime = lastUpdateTime;
            this.deleteUser = deleteUser;
            this.deleteTime = deleteTime;
            this.refs = refs;
            this.authorization = authorization;
            this.authorizationCount = Stream.ofAll(authorization).size();
        }
        //endregion

        //region Properties
        public Date getCreationTime() {
            return creationTime;
        }

        public String getCreationUser() {
            return creationUser;
        }

        public Date getLastUpdateTime() {
            return lastUpdateTime;
        }

        public String getLastUpdateUser() {
            return lastUpdateUser;
        }

        public Date getDeleteTime() {
            return deleteTime;
        }

        public String getDeleteUser() {
            return deleteUser;
        }

        public Iterable<String> getRefs() {
            return refs;
        }

        public Iterable<String> getAuthorization() {
            return authorization;
        }

        public int getAuthorizationCount() {
            return authorizationCount;
        }
        //endregion

        //region Fields
        private Date creationTime;
        private String creationUser;

        private Date lastUpdateTime;
        private String lastUpdateUser;

        private Date deleteTime;
        private String deleteUser;

        private Iterable<String> refs;

        private Iterable<String> authorization;
        private int authorizationCount;
        //endregion
    }

    //region Constructors
    public KnowledgeEntityBase() {}

    public KnowledgeEntityBase(String type) {
        this.type = type;
    }

    public KnowledgeEntityBase(String type, Metadata metadata) {
        this.type = type;

        if (metadata != null) {
            this.creationUser = metadata.getCreationUser();
            this.creationTime = metadata.getCreationTime();
            this.lastUpdateUser = metadata.getLastUpdateUser();
            this.lastUpdateTime = metadata.getLastUpdateTime();
            this.deleteUser = metadata.getDeleteUser();
            this.deleteTime = metadata.getDeleteTime();
            this.authorization = metadata.getAuthorization();
            this.authorizationCount = metadata.getAuthorizationCount();
        }
    }
    //endregion

    //region Properties
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public Date getCreationTime() {
        return creationTime;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public Date getDeleteTime() {
        return deleteTime;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(String deleteUser) {
        this.deleteUser = deleteUser;
    }

    public Iterable<String> getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Iterable<String> authorization) {
        this.authorization = authorization;
    }

    public int getAuthorizationCount() {
        return authorizationCount;
    }

    public void setAuthorizationCount(int authorizationCount) {
        this.authorizationCount = authorizationCount;
    }
    //endregion

    //region Fields
    private String type;

    private Date creationTime;
    private String creationUser;

    private Date lastUpdateTime;
    private String lastUpdateUser;

    private Date deleteTime;
    private String deleteUser;

    private Iterable<String> authorization;
    private int authorizationCount;
    //endregion
}
