package com.kayhut.fuse.generator.model.entity;

/*-
 * #%L
 * fuse-domain-gragons-datagen
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

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Guild extends EntityBase {

    //region Ctrs
    public Guild() {
    }

    public Guild(String id, String name) {
        super(id);
        this.name = name;
    }
    //endregion

    //region Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(Date establishDate) {
        this.establishDate = establishDate;
    }
    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[] { this.getId(),
                this.name,
                this.description,
                this.iconId,
                this.url,
                Long.toString(this.establishDate.getTime())};
    }
    //endregion

    //region Fields
    private String name;
    private String description;
    private String iconId;
    private String url;
    private Date establishDate;
    //endregion

    //region Builder
    public static final class Builder {
        String name;
        String description;
        String iconId;
        String url;
        Date establishDate;
        private String id;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withIconId(String iconId) {
            this.iconId = iconId;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withEstablishDate(Date establishDate) {
            this.establishDate = establishDate;
            return this;
        }

        public Guild build() {
            Guild guild = new Guild();
            guild.setId(id);
            guild.setName(name);
            guild.setDescription(description);
            guild.setIconId(iconId);
            guild.setUrl(url);
            guild.setEstablishDate(establishDate);
            return guild;
        }
    }
    //endregion

}
