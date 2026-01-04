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

    private static final String DEFAULT_INPUT_FILE = "complex_expressions-limited.json";
    private static final String OUTPUT_DIR = "output";
    private static final String TIMINGS_FILE = "test_timings.md";
    private static final String FAILED_FILE = "failed_tests.md";

    /**
     * CLI configuration record.
     */
    private record CliConfig(String inputFile, int iterations, boolean isClasspathResource) {}

    public static void main(String[] args) {
        try {
            // Parse CLI arguments
            CliConfig config = parseCliArguments(args);

            System.out.println("Loading expressions from: " + config.inputFile);

            // Load expressions based on source type
            ExpressionLoader.LoadedExpressions loaded = config.isClasspathResource
                ? ExpressionLoader.loadExpressionsFromClasspath(config.inputFile)
                : ExpressionLoader.loadExpressionsFromFile(config.inputFile);

            List<ExpressionData> expressions = loaded.expressions();
            String absolutePath = loaded.absolutePath();

            System.out.println("Loaded " + expressions.size() + " expressions");
            System.out.println("Resolved path: " + absolutePath);

            System.out.println("Running load test with " + config.iterations + " iteration(s)...");

            LoadTester tester = new LoadTester(config.iterations, absolutePath);
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
     * Parses command line arguments to get configuration.
     *
     * @param args command line arguments
     * @return CLI configuration
     * @throws IllegalArgumentException if arguments are invalid
     */
    private static CliConfig parseCliArguments(String[] args) {
        String inputFile = DEFAULT_INPUT_FILE;
        int iterations = 1;
        boolean isClasspathResource = true;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--input":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--input requires a file path");
                    }
                    inputFile = args[++i];
                    isClasspathResource = false;
                    break;
                case "--iterations":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--iterations requires a value");
                    }
                    iterations = parseInt(args[++i]);
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unknown argument: " + args[i] + "\n" + getUsageMessage()
                    );
            }
        }

        return new CliConfig(inputFile, iterations, isClasspathResource);
    }

    /**
     * Parses an integer value with validation.
     *
     * @param value the string value to parse
     * @return the parsed integer
     * @throws IllegalArgumentException if value is invalid
     */
    private static int parseInt(String value) {
        try {
            int result = Integer.parseInt(value);
            if (result < 1) {
                throw new IllegalArgumentException("Iterations must be >= 1");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid iterations value: " + value);
        }
    }

    /**
     * Returns the usage message.
     *
     * @return usage message string
     */
    private static String getUsageMessage() {
        return "Usage: java Main [--input FILE] [--iterations N]\n" +
               "  --input FILE       Path to expressions JSON file\n" +
               "                     - Absolute path: /home/user/data/file.json\n" +
               "                     - Relative path: file.json (resolved to src/main/resources/file.json)\n" +
               "                     - Default: complex_expressions-limited.json (from classpath)\n" +
               "  --iterations N     Number of iterations (default: 1, must be >= 1)";
    }
}
