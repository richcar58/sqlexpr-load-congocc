package net.magneticpotato.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.magneticpotato.model.ExpressionData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads expression data from JSON files using streaming for efficiency.
 */
public class ExpressionLoader {

    /**
     * Loads expressions from a classpath resource.
     *
     * @param resourcePath the path to the resource (should start with /)
     * @return list of loaded expressions
     * @throws IOException if the resource cannot be read
     */
    public static List<ExpressionData> loadExpressions(String resourcePath) throws IOException {
        List<ExpressionData> expressions = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = ExpressionLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

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
        }

        return expressions;
    }
}
