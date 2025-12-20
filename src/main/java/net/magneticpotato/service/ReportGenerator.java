package net.magneticpotato.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.magneticpotato.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Generates markdown reports for load test results.
 */
public class ReportGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");

    /**
     * Generates the timings report.
     *
     * @param outputPath path to the output file
     * @param overall overall statistics
     * @param classStats per-complexity-class statistics
     * @throws IOException if the file cannot be written
     */
    public static void generateTimingsReport(
        Path outputPath,
        OverallStatistics overall,
        List<ClassStatistics> classStats
    ) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("# Load Test Results - sqlexpr-congocc\n\n");

        // Overall Statistics
        sb.append("## Overall Statistics\n\n");
        sb.append(formatOverallStats(overall));
        sb.append("\n");

        // Per-complexity-class stats
        for (ClassStatistics stats : classStats) {
            sb.append(formatClassStats(stats));
            sb.append("\n");
        }

        // Footer
        sb.append("---\n\n");
        sb.append("Generated: ");
        sb.append(ZonedDateTime.now().format(TIMESTAMP_FORMATTER));
        sb.append("\n");

        // Write to file
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, sb.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Generates the failed tests report.
     *
     * @param outputPath path to the output file
     * @param failures list of failed evaluations
     * @throws IOException if the file cannot be written
     */
    public static void generateFailedTestsReport(
        Path outputPath,
        List<FailedEvaluation> failures
    ) throws IOException {
        if (failures.isEmpty()) {
            // Don't create file if no failures
            return;
        }

        StringBuilder sb = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        sb.append("# Failed Evaluations - sqlexpr-congocc\n\n");

        for (int i = 0; i < failures.size(); i++) {
            FailedEvaluation failure = failures.get(i);

            sb.append("## Failure ").append(i + 1).append("\n\n");

            sb.append("**Expression:**\n```\n");
            sb.append(failure.expression()).append("\n");
            sb.append("```\n\n");

            sb.append("**Value Map:**\n```json\n");
            sb.append(mapper.writeValueAsString(failure.valueMap())).append("\n");
            sb.append("```\n\n");

            sb.append("**Error:**\n```\n");
            sb.append(failure.errorMessage()).append("\n");
            sb.append("```\n\n");

            sb.append("---\n\n");
        }

        sb.append("Generated: ");
        sb.append(ZonedDateTime.now().format(TIMESTAMP_FORMATTER));
        sb.append("\n");

        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, sb.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Formats the overall statistics section.
     */
    private static String formatOverallStats(OverallStatistics stats) {
        StringBuilder sb = new StringBuilder();

        sb.append("- **Start Time (Local)**: ")
            .append(stats.startTimeLocal().format(TIMESTAMP_FORMATTER))
            .append("\n");
        sb.append("- **Start Time (UTC)**: ")
            .append(stats.startTimeUtc().format(TIMESTAMP_FORMATTER))
            .append("\n");
        sb.append("- **Iterations**: ")
            .append(stats.iterations())
            .append("\n");
        sb.append("- **Total Unique Expressions**: ")
            .append(stats.totalUniqueExpressions())
            .append("\n");
        sb.append("- **Total Evaluations Executed**: ")
            .append(stats.totalEvaluations())
            .append("\n");
        sb.append("- **Total Execution Time**: ")
            .append(String.format("%.3f ms", stats.totalExecutionTimeMs()))
            .append("\n");
        sb.append("- **Failed Evaluations**: ")
            .append(stats.failedEvaluations())
            .append("\n");

        return sb.toString();
    }

    /**
     * Formats a complexity class statistics section.
     */
    private static String formatClassStats(ClassStatistics stats) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Complexity Class: ").append(stats.complexity()).append("\n\n");

        // Define metrics
        String[] metricNames = {
            "Total Time",
            "Min Time",
            "Max Time",
            "Average Time",
            "Std Deviation"
        };

        double[] values = {
            stats.totalTimeMs(),
            stats.minTimeMs(),
            stats.maxTimeMs(),
            stats.avgTimeMs(),
            stats.stdDevMs()
        };

        // Calculate column widths
        int metricWidth = Arrays.stream(metricNames)
            .mapToInt(String::length)
            .max()
            .orElse(10);
        metricWidth = Math.max(metricWidth, "Metric".length()) + 2;

        int valueWidth = 14;  // Fixed width for formatted numbers

        // Table header
        sb.append("| ").append(pad("Metric", metricWidth)).append(" | ");
        sb.append(pad("Milliseconds", valueWidth)).append(" |\n");

        // Separator
        sb.append("|").append("-".repeat(metricWidth + 2)).append("|");
        sb.append("-".repeat(valueWidth + 2)).append("|\n");

        // Table rows
        for (int i = 0; i < metricNames.length; i++) {
            sb.append("| ").append(pad(metricNames[i], metricWidth)).append(" | ");
            sb.append(String.format("%14.6f", values[i])).append(" |\n");
        }

        sb.append("\n");
        sb.append("- **Total Evaluations**: ").append(stats.totalEvaluations()).append("\n");
        sb.append("- **Unique Expressions**: ").append(stats.uniqueExpressions()).append("\n");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Pads a string to the specified width.
     */
    private static String pad(String str, int width) {
        return String.format("%-" + width + "s", str);
    }
}
