package com.main.face_recognition_resource_server.constants;

public enum ControlRoutingType {
    SHIFT("shift"),
    USER("user");

    private final String value;

    ControlRoutingType(String value) {
        this.value = value;
    }

    public static ControlRoutingType fromValue(String value) {
        for (ControlRoutingType type : ControlRoutingType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("invalid control routing type");
    }

    public String getValue() {
        return value;
    }

}
