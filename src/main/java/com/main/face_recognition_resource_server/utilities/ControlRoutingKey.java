package com.main.face_recognition_resource_server.utilities;

import com.main.face_recognition_resource_server.constants.ControlRoutingType;
import lombok.Getter;

public class ControlRoutingKey {
    @Getter
    private final ControlRoutingType controlRoutingType;
    @Getter
    private final Long organizationId;

    public ControlRoutingKey(String routingKey) {
        if (routingKey == null || routingKey.isEmpty()) {
            throw new IllegalArgumentException("invalid routing key");
        }

        int ROUTING_KEY_CONTROL_PREFIX_INDEX = 0;
        int ROUTING_KEY_ORGANIZATION_ID_INDEX = 1;
        int ROUTING_KEY_ROUTING_TYPE_INDEX = 2;
        int ROUTING_KEY_SUFFIX_INDEX = 3;
        int ROUTING_KEY_LENGTH = 4;

        String ROUTING_KEY_CONTROL_PREFIX = "control";
        String ROUTING_KEY_SUFFIX = "key";

        String[] routingKeyParts = routingKey.split("\\.");
        organizationId = Long.valueOf(routingKeyParts[ROUTING_KEY_ORGANIZATION_ID_INDEX]);

        if (!routingKeyParts[ROUTING_KEY_CONTROL_PREFIX_INDEX].equals(ROUTING_KEY_CONTROL_PREFIX) ||
                routingKeyParts.length != ROUTING_KEY_LENGTH || !routingKeyParts[ROUTING_KEY_SUFFIX_INDEX].equals(ROUTING_KEY_SUFFIX)
        ) {
            throw new IllegalArgumentException("invalid routing key");
        }
        controlRoutingType = ControlRoutingType.fromValue(routingKeyParts[ROUTING_KEY_ROUTING_TYPE_INDEX]);
    }
}
