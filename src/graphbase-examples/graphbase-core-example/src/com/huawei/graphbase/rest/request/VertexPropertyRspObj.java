package com.huawei.graphbase.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VertexPropertyRspObj {
    private String id;

    @JsonProperty("propertyName")
    private String name;

    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PropertyRspObj{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", value='" + value + '\'' + '}';
    }
}
