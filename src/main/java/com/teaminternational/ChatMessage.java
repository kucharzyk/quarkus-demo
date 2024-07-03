package com.teaminternational;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@RegisterForReflection
public record ChatMessage(String message, @JsonProperty("HEADERS") Map<String, Object> headers) {
}
