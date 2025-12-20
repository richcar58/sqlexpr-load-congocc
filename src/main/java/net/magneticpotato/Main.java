package net.magneticpotato;

import net.magneticpotato.model.ExpressionData;
import net.magneticpotato.model.LoadTestResult;
import net.magneticpotato.service.ExpressionLoader;
import net.magneticpotato.service.LoadTester;
import net.magneticpotato.service.ReportGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main entry point for the SQL expression load testing program.
 */
public class Main {

    private static final String JSON_RESOURCE = "/complex_expressions.json";
    private static final String OUTPUT_DIR = "output";
    private static final String TIMINGS_FILE = "test_timings.md";
    private static final String FAILED_FILE = "failed_tests.md";

    public static void main(String[] args) {
        try {
            // Parse CLI arguments
            int iterations = parseIterations(args);

            System.out.println("Loading expressions from JSON...");
            List<ExpressionData> expressions = ExpressionLoader.loadExpressions(JSON_RESOURCE);
            System.out.println("Loaded " + expressions.size() + " expressions");

            System.out.println("Running load test with " + iterations + " iteration(s)...");

            LoadTester tester = new LoadTester(iterations);
            LoadTestResult result = tester.run(expressions);

            System.out.println("Generating reports...");

            Path outputDir = Paths.get(OUTPUT_DIR);
            Path timingsPath = outputDir.resolve(TIMINGS_FILE);
            Path failedPath = outputDir.resolve(FAILED_FILE);

            ReportGenerator.generateTimingsReport(
                timingsPath,
                result.overallStats(),
                result.classStats()
            );

            ReportGenerator.generateFailedTestsReport(
                failedPath,
                result.failures()
            );

            System.out.println("Reports generated:");
            System.out.println("  - " + timingsPath);
            if (!result.failures().isEmpty()) {
                System.out.println("  - " + failedPath);
                System.out.println("WARNING: " + result.failures().size() + " evaluations failed!");
            }

            System.out.println("\nTest completed successfully.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parses command line arguments to get the iterations value.
     *
     * @param args command line arguments
     * @return the number of iterations
     * @throws IllegalArgumentException if arguments are invalid
     */
    private static int parseIterations(String[] args) {
        if (args.length == 0) {
            return 1;  // Default
        }

        if (args.length == 2 && "--iterations".equals(args[0])) {
            try {
                int iterations = Integer.parseInt(args[1]);
                if (iterations < 1) {
                    throw new IllegalArgumentException("Iterations must be >= 1");
                }
                return iterations;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid iterations value: " + args[1]);
            }
        }

        throw new IllegalArgumentException(
            "Usage: java Main [--iterations N]\n" +
            "  --iterations N  Number of iterations (default: 1, must be >= 1)"
        );
    }
}