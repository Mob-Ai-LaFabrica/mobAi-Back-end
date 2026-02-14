package org.example.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskType {
    RECEIPT,
    TRANSFER,
    PICKING,
    DELIVERY;

    @JsonValue
    public String toJson() {
        return name();
    }

    @JsonCreator
    public static TaskType fromJson(String value) {
        if (value == null) {
            return null;
        }
        return TaskType.valueOf(value.trim().toUpperCase());
    }
}