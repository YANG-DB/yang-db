package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AttachedProperty {

    public void setPName (String pName)
    {
        this.pName = pName;
    }

    public Object getValue ()
    {
        return value;
    }

    public void setValue (Object value)
    {
        this.value = value;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString()
    {
        return "AttachedProperty [pName = "+pName+", value = "+value+"]";
    }

    //region Fields
    private String pName;
    private Object value;
    private String tag;
    //endregion

    public static final class AttachedPropertyBuilder {
        private String pName;
        private Object value;
        private String tag;

        private AttachedPropertyBuilder() {
        }

        public static AttachedPropertyBuilder anAttachedProperty() {
            return new AttachedPropertyBuilder();
        }

        public AttachedPropertyBuilder withPName(String pName) {
            this.pName = pName;
            return this;
        }

        public AttachedPropertyBuilder withValue(Object value) {
            this.value = value;
            return this;
        }

        public AttachedPropertyBuilder withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public AttachedProperty build() {
            AttachedProperty attachedProperty = new AttachedProperty();
            attachedProperty.setPName(pName);
            attachedProperty.setValue(value);
            attachedProperty.setTag(tag);
            return attachedProperty;
        }
    }


}
