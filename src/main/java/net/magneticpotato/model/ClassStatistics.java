package net.magneticpotato.model;

import java.util.List;

/**
 * Computed statistics for a complexity class.
 */
public record ClassStatistics(
    int complexity,
    int totalEvaluations,
    int uniqueExpressions,
    double totalTimeMs,
    double minTimeMs,
    double maxTimeMs,
    double avgTimeMs,
    double stdDevMs
) {
    /**
     * Creates ClassStatistics from ClassTimings data.
     */
    public static ClassStatistics fromTimings(int complexity, ClassTimings timings) {
        double min = (timings.getMinTime() == Double.MAX_VALUE) ? 0.0 : timings.getMinTime();
        double avg = (timings.getEvaluationCount() > 0)
            ? timings.getAccumulatedTime() / timings.getEvaluationCount()
            : 0.0;
        double stdDev = calculateStdDev(timings.getIndividualTimings(), avg);

        return new ClassStatistics(
            complexity,
            timings.getEvaluationCount(),
            timings.getUniqueExprCount(),
            timings.getAccumulatedTime(),
            min,
            timings.getMaxTime(),
            avg,
            stdDev
        );
    }

    /**
     * Calculates standard deviation using Bessel's correction (n-1 denominator).
     */
    private static double calculateStdDev(List<Double> timings, double mean) {
        if (timings.size() <= 1) {
            return 0.0;
        }

        double sumSquaredDiffs = timings.stream()
            .mapToDouble(t -> Math.pow(t - mean, 2))
            .sum();

        return Math.sqrt(sumSquaredDiffs / (timings.size() - 1));
    }
}
