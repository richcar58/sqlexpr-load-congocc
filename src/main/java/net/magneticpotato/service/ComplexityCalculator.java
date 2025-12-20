package net.magneticpotato.service;

/**
 * Calculates the complexity of a SQL boolean expression.
 * Complexity is defined as the number of logical AND and OR operators,
 * excluding AND operators that are part of BETWEEN clauses.
 */
public class ComplexityCalculator {

    /**
     * Calculates the complexity of an expression.
     *
     * @param expression the SQL boolean expression
     * @return the complexity (count of AND and OR operators)
     */
    public static int calculate(String expression) {
        String upperExpr = expression.toUpperCase();
        String cleaned = removeBetweenAnd(upperExpr);
        int andCount = countWordOccurrences(cleaned, " AND ");
        int orCount = countWordOccurrences(cleaned, " OR ");
        return andCount + orCount;
    }

    /**
     * Removes AND operators that are part of BETWEEN clauses by replacing them
     * with placeholders.
     *
     * @param expr the expression in uppercase
     * @return the expression with BETWEEN AND operators removed
     */
    private static String removeBetweenAnd(String expr) {
        StringBuilder result = new StringBuilder(expr);
        int pos = 0;

        while ((pos = result.indexOf(" BETWEEN ", pos)) != -1) {
            // Replace " BETWEEN " with " _______ " (same length)
            result.replace(pos, pos + 9, " _______ ");

            // Find the next " AND " after this BETWEEN position
            int andPos = result.indexOf(" AND ", pos + 9);

            if (andPos != -1) {
                // Verify this AND is part of the BETWEEN clause by checking
                // that there's no OR, AND, or BETWEEN between them
                String between = result.substring(pos + 9, andPos);
                if (!between.contains(" OR ") && !between.contains(" AND ") &&
                    !between.contains(" BETWEEN ")) {
                    // This AND belongs to the BETWEEN clause
                    result.replace(andPos, andPos + 5, " ___ ");
                }
            }

            pos += 9;
        }

        return result.toString();
    }

    /**
     * Counts occurrences of a word in the text.
     *
     * @param text the text to search
     * @param word the word to count (should include surrounding spaces)
     * @return the count of occurrences
     */
    private static int countWordOccurrences(String text, String word) {
        int count = 0;
        int pos = 0;

        while ((pos = text.indexOf(word, pos)) != -1) {
            count++;
            pos += word.length();
        }

        return count;
    }
}
