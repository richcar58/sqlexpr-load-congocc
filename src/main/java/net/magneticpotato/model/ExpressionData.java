package net.magneticpotato.model;

import java.util.Map;

/**
 * Represents a test expression loaded from JSON.
 */
public record ExpressionData(
    String expr,
    Map<String, Object> value_map
) {}
