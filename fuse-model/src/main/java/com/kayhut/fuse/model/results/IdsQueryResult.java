package com.kayhut.fuse.model.results;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

public class IdsQueryResult extends QueryResultBase {

    public IdsQueryResult() {
    }

    public IdsQueryResult(List<String> ids) {
        this.ids = ids;
    }

    public String getResultType() {
        return "ids";
    }


    @Override
    public int getSize() {
        return this.ids.size();
    }

    private List<String> ids;

    public static final class Builder {
        public Builder() {
        }

        public static Builder instance() {
            return new Builder();
        }

        public Builder withIds(List<String> ids) {
            this.ids = ids;
            return this;
        }

        public IdsQueryResult build() {
            return new IdsQueryResult(this.ids);
        }

        private List<String> ids;
    }
}
