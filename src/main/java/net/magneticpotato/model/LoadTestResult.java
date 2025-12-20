package net.magneticpotato.model;

import java.util.List;

/**
 * Container for all load test results.
 */
public record LoadTestResult(
    OverallStatistics overallStats,
    List<ClassStatistics> classStats,
    List<FailedEvaluation> failures
) {}
