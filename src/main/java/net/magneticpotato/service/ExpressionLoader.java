package net.magneticpotato.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.magneticpotato.model.ExpressionData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads expression data from JSON files using streaming for efficiency.
 * Supports loading from both classpath resources and file system paths.
 */
public class ExpressionLoader {

    /**
     * Container for loaded expressions with resolved path information.
     */
    public record LoadedExpressions(
        List<ExpressionData> expressions,
        String absolutePath
    ) {}

    /**
     * Loads expressions from a classpath resource.
     *
     * @param resourcePath the path to the resource (e.g., "complex_expressions.json")
     * @return loaded expressions with resolved absolute path
     * @throws IOException if the resource cannot be read
     */
    public static LoadedExpressions loadExpressionsFromClasspath(String resourcePath) throws IOException {
        List<ExpressionData> expressions;
        ObjectMapper mapper = new ObjectMapper();

        // Normalize resource path (ensure leading slash)
        String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;

        try (InputStream is = ExpressionLoader.class.getResourceAsStream(normalizedPath)) {
            if (is == null) {
                throw new IOException("Classpath resource not found: " + normalizedPath);
            }

            expressions = parseJsonStream(is, mapper);
        }

        // Resolve absolute path for classpath resources
        String absolutePath = resolveClasspathResourcePath(normalizedPath);

        return new LoadedExpressions(expressions, absolutePath);
    }

    /**
     * Loads expressions from a filesystem file.
     * Supports both absolute and relative paths.
     * Relative paths are resolved relative to src/main/resources/ directory.
     *
     * @param filePath the path to the file (absolute or relative)
     * @return loaded expressions with absolute resolved path
     * @throws IOException if the file cannot be read
     */
    public static LoadedExpressions loadExpressionsFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // If relative path, resolve relative to src/main/resources
        if (!path.isAbsolute()) {
            path = Paths.get("src/main/resources", filePath);
        }

        // Validate file exists and is readable
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + path.toAbsolutePath());
        }
        if (!Files.isReadable(path)) {
            throw new IOException("File not readable: " + path.toAbsolutePath());
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Not a regular file: " + path.toAbsolutePath());
        }

        String absolutePath = path.toAbsolutePath().toString();
        List<ExpressionData> expressions;
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = Files.newInputStream(path)) {
            expressions = parseJsonStream(is, mapper);
        }

        return new LoadedExpressions(expressions, absolutePath);
    }

    /**
     * Parses JSON array from input stream using streaming API.
     *
     * @param is the input stream containing JSON data
     * @param mapper the Jackson ObjectMapper
     * @return list of parsed expressions
     * @throws IOException if JSON parsing fails
     */
    private static List<ExpressionData> parseJsonStream(InputStream is, ObjectMapper mapper)
            throws IOException {
        List<ExpressionData> expressions = new ArrayList<>();

        JsonFactory factory = mapper.getFactory();
        try (JsonParser parser = factory.createParser(is)) {
            // Expect array start
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected array start in JSON file");
            }

            // Parse each object in the array
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                ExpressionData expr = mapper.readValue(parser, ExpressionData.class);
                expressions.add(expr);
            }
        }

        return expressions;
    }

    /**
     * Resolves the absolute filesystem path of a classpath resource.
     * Returns a descriptive path if actual filesystem path cannot be determined.
     *
     * @param resourcePath the classpath resource path
     * @return absolute path or descriptive string
     */
    private static String resolveClasspathResourcePath(String resourcePath) {
        try {
            URL resourceUrl = ExpressionLoader.class.getResource(resourcePath);
            if (resourceUrl != null) {
                if ("file".equals(resourceUrl.getProtocol())) {
                    // File on filesystem (e.g., target/classes/file.json)
                    return Paths.get(resourceUrl.toURI()).toAbsolutePath().toString();
                } else if ("jar".equals(resourceUrl.getProtocol())) {
                    // Inside JAR file
                    return resourceUrl.toString();
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return "classpath:" + resourcePath;
    }

    /**
     * Legacy method for backward compatibility.
     * Loads expressions from a classpath resource.
     *
     * @param resourcePath the path to the resource (should start with /)
     * @return list of loaded expressions
     * @throws IOException if the resource cannot be read
     * @deprecated Use {@link #loadExpressionsFromClasspath(String)} instead
     */
    @Deprecated
    public static List<ExpressionData> loadExpressions(String resourcePath) throws IOException {
        return loadExpressionsFromClasspath(resourcePath).expressions();
    }
}
