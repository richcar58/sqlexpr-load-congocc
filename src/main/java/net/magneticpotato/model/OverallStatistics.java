package net.magneticpotato.model;

import java.time.ZonedDateTime;

/**
 * Overall statistics for the entire load test execution.
 */
public record OverallStatistics(
    String inputFilePath,
    ZonedDateTime startTimeLocal,
    ZonedDateTime startTimeUtc,
    int iterations,
    int totalUniqueExpressions,
    long totalEvaluations,
    double totalExecutionTimeMs,
    int failedEvaluations
) {}
