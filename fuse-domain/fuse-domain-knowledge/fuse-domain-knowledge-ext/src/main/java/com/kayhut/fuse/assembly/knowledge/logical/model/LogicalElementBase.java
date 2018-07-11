package com.kayhut.fuse.assembly.knowledge.logical.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class LogicalElementBase {
    public static class Metadata {
        public static class Authorization {
            public Authorization(String authorization) {
                // TODO: fix auth
                String[] authParts = new String[2];
                this.s = authParts[0];
                this.s = authParts[1];
            }

            private String s;
            private String pp;

            public String getS() {
                return s;
            }

            public String getPP() {
                return pp;
            }
        }

        //region Constructors
        public Metadata(
                String creationUser,
                Date creationTime,
                String lastUpdateUser,
                Date lastUpdateTime,
                List<Authorization> authorization) {
            this(creationUser, creationTime, lastUpdateUser, lastUpdateTime, null, null, authorization);
        }

        public Metadata(
                String creationUser,
                Date creationTime,
                String lastUpdateUser,
                Date lastUpdateTime,
                String deleteUser,
                Date deleteTime,
                List<Authorization> authorization) {
            this.creationUser = creationUser;
            this.creationTime = creationTime;
            this.lastUpdateUser = lastUpdateUser;
            this.lastUpdateTime = lastUpdateTime;
            this.deleteUser = deleteUser;
            this.deleteTime = deleteTime;
            this.authorization = authorization;
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

        public List<Authorization> getAuthorization() {
            return authorization;
        }

        //endregion

        //region Fields
        private Date creationTime;
        private String creationUser;

        private Date lastUpdateTime;
        private String lastUpdateUser;

        private Date deleteTime;
        private String deleteUser;

        private List<Authorization> authorization;
        //endregion
    }

    //region Constructors

    public LogicalElementBase(Metadata metadata) {
        this.metadata = metadata;
    }

    //endregion

    //region Properties
    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    //endregion

    //region Fields
    private Metadata metadata;
    //endregion
}
