package net.magneticpotato.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks timing metrics for a single complexity class.
 */
public class ClassTimings {
    private double accumulatedTime;
    private double minTime;
    private double maxTime;
    private int evaluationCount;
    private int uniqueExprCount;
    private final List<Double> individualTimings;

    public ClassTimings() {
        this.accumulatedTime = 0.0;
        this.minTime = Double.MAX_VALUE;
        this.maxTime = 0.0;
        this.evaluationCount = 0;
        this.uniqueExprCount = 0;
        this.individualTimings = new ArrayList<>();
    }

    public void addTiming(double durationMs) {
        accumulatedTime += durationMs;
        minTime = Math.min(minTime, durationMs);
        maxTime = Math.max(maxTime, durationMs);
        evaluationCount++;
        individualTimings.add(durationMs);
    }

    public void incrementUniqueExpr() {
        uniqueExprCount++;
    }

    public double getAccumulatedTime() {
        return accumulatedTime;
    }

    public double getMinTime() {
        return minTime;
    }

    public double getMaxTime() {
        return maxTime;
    }

    public int getEvaluationCount() {
        return evaluationCount;
    }

    public int getUniqueExprCount() {
        return uniqueExprCount;
    }

    public List<Double> getIndividualTimings() {
        return individualTimings;
    }
}
