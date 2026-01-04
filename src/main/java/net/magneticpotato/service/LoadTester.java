package net.magneticpotato.service;

import net.magneticpotato.model.*;
import net.magneticpotato.sqlexpr.congocc.SqlExprEvaluator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates the load testing of SQL expressions.
 */
public class LoadTester {
    private final int iterations;
    private final String inputFilePath;
    private final Map<Integer, ClassTimings> classTimingsMap;
    private final List<FailedEvaluation> failedEvaluations;
    private final Map<String, Integer> complexityCache;
    private final Set<String> failedExpressions;

    public LoadTester(int iterations, String inputFilePath) {
        this.iterations = iterations;
        this.inputFilePath = inputFilePath;
        this.classTimingsMap = new TreeMap<>();
        this.failedEvaluations = new ArrayList<>();
        this.complexityCache = new HashMap<>();
        this.failedExpressions = new HashSet<>();
    }

    /**
     * Runs the load test on the provided expressions.
     *
     * @param expressions list of expressions to test
     * @return load test results
     */
    public LoadTestResult run(List<ExpressionData> expressions) {
        ZonedDateTime startLocal = ZonedDateTime.now();
        ZonedDateTime startUtc = startLocal.withZoneSameInstant(ZoneId.of("UTC"));

        // Pre-calculate complexities and initialize timings
        for (ExpressionData expr : expressions) {
            int complexity = ComplexityCalculator.calculate(expr.expr());
            complexityCache.put(expr.expr(), complexity);
            classTimingsMap.computeIfAbsent(complexity, k -> new ClassTimings());
        }

        // Mark unique expressions in each class
        for (ExpressionData expr : expressions) {
            int complexity = complexityCache.get(expr.expr());
            classTimingsMap.get(complexity).incrementUniqueExpr();
        }

        // Run iterations
        for (int iter = 0; iter < iterations; iter++) {
            if (iterations > 1 && (iter + 1) % 10 == 0) {
                System.out.println("  Completed iteration " + (iter + 1) + "/" + iterations);
            }

            for (ExpressionData expr : expressions) {
                // Skip if this expression has already failed
                if (failedExpressions.contains(expr.expr())) {
                    continue;
                }

                evaluateExpression(expr);
            }
        }

        // Compute statistics
        List<ClassStatistics> classStats = classTimingsMap.entrySet().stream()
            .map(entry -> ClassStatistics.fromTimings(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        // Compute overall statistics
        int totalUniqueExpr = classStats.stream()
            .mapToInt(ClassStatistics::uniqueExpressions)
            .sum();

        long totalEvaluations = classStats.stream()
            .mapToLong(ClassStatistics::totalEvaluations)
            .sum();

        double totalTime = classStats.stream()
            .mapToDouble(ClassStatistics::totalTimeMs)
            .sum();

        OverallStatistics overall = new OverallStatistics(
            inputFilePath,
            startLocal,
            startUtc,
            iterations,
            totalUniqueExpr,
            totalEvaluations,
            totalTime,
            failedEvaluations.size()
        );

        return new LoadTestResult(overall, classStats, failedEvaluations);
    }

    /**
     * Evaluates a single expression and records timing.
     *
     * @param expr the expression to evaluate
     */
    private void evaluateExpression(ExpressionData expr) {
        int complexity = complexityCache.get(expr.expr());
        ClassTimings timings = classTimingsMap.get(complexity);

        long startNs = System.nanoTime();
        try {
            boolean result = SqlExprEvaluator.match(expr.expr(), expr.value_map());
            long endNs = System.nanoTime();

            double durationMs = (endNs - startNs) / 1_000_000.0;
            timings.addTiming(durationMs);

        } catch (Exception e) {
            long endNs = System.nanoTime();
            double durationMs = (endNs - startNs) / 1_000_000.0;

            // Record timing even for failures
            timings.addTiming(durationMs);

            // Mark as failed and add to failed list
            failedExpressions.add(expr.expr());
            failedEvaluations.add(new FailedEvaluation(
                expr.expr(),
                expr.value_map(),
                e.getClass().getSimpleName() + ": " + e.getMessage()
            ));
        }
    }
}
