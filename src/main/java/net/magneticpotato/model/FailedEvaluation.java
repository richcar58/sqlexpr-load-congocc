package net.magneticpotato.model;

import java.util.Map;

/**
 * Captures details of a failed expression evaluation.
 */
public record FailedEvaluation(
    String expression,
    Map<String, Object> valueMap,
    String errorMessage
) {}
