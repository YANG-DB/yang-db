package com.kayhut.fuse.unipop.schemaProviders;

/**
 * Created by moti on 4/27/2017.
 */
public interface GraphElementPropertySchema {
    class Impl implements GraphElementPropertySchema {
        //region Constructors
        public Impl(String name, String type) {
            this.name = name;
            this.type = type;
        }
        //endregion

        //region GraphElementPropertySchema Implementation
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getType() {
            return this.type;
        }
        //endregion

        //region Fields
        private String name;
        private String type;
        //endregion
    }

    String getName();
    String getType();
}
