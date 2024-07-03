package com.teaminternational;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ChatMessage(String message, @JsonProperty("HEADERS") Map<String, Object> headers) {
}
