package net.magneticpotato.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for ComplexityCalculator to verify AND/OR counting with BETWEEN exclusion.
 */
public class ComplexityCalculatorTest {

    @Test
    void testSimpleAndOr() {
        assertEquals(2, ComplexityCalculator.calculate("a = 1 AND b = 2 OR c = 3"));
    }

    @Test
    void testBetweenExclusion() {
        assertEquals(0, ComplexityCalculator.calculate("f92 BETWEEN 20 AND 30"));
    }

    @Test
    void testNotBetweenExclusion() {
        assertEquals(0, ComplexityCalculator.calculate("f92 NOT BETWEEN 20 AND 30"));
    }

    @Test
    void testBetweenWithLogicalAnd() {
        assertEquals(1, ComplexityCalculator.calculate("(a BETWEEN 1 AND 10) AND (b = 5)"));
    }

    @Test
    void testBetweenWithLogicalOr() {
        assertEquals(1, ComplexityCalculator.calculate("(a BETWEEN 1 AND 10) OR (b = 5)"));
    }

    @Test
    void testMultipleBetween() {
        assertEquals(1, ComplexityCalculator.calculate("(a BETWEEN 1 AND 10) AND (b BETWEEN 20 AND 30)"));
    }

    @Test
    void testComplexExpression() {
        // From the test data: "(s76 NOT LIKE '_sometext') AND (f92 NOT BETWEEN 20 AND 30)"
        // Should count only the logical AND, not the BETWEEN AND
        assertEquals(1, ComplexityCalculator.calculate("(s76 NOT LIKE '_sometext') AND (f92 NOT BETWEEN 20 AND 30)"));
    }

    @Test
    void testCaseInsensitive() {
        assertEquals(1, ComplexityCalculator.calculate("a = 1 and b = 2"));
        assertEquals(1, ComplexityCalculator.calculate("a = 1 AnD b = 2"));
    }

    @Test
    void testNoOperators() {
        assertEquals(0, ComplexityCalculator.calculate("x = 5"));
    }

    @Test
    void testOnlyOr() {
        assertEquals(2, ComplexityCalculator.calculate("a = 1 OR b = 2 OR c = 3"));
    }

    @Test
    void testOnlyAnd() {
        assertEquals(2, ComplexityCalculator.calculate("a = 1 AND b = 2 AND c = 3"));
    }
}
