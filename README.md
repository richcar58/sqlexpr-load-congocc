# sqlexpr-load-congocc

Load testing and performance evaluation program for the [sqlexpr-congocc](https://github.com/richcar58/sqlexpr-congocc) SQL expression evaluator library.

## Overview

This program benchmarks the sqlexpr-congocc expression evaluator by:
- Loading test expressions from JSON files
- Calculating expression complexity (AND/OR operator count, excluding BETWEEN clauses)
- Timing each evaluation with nanosecond precision
- Generating detailed performance statistics and markdown reports

Results can be compared with other implementations, such as the [Rust version](https://github.com/richcar58/sqlexpr-load-rust).

## Quick Start

**Build:**
```bash
mvn clean package
```

**Run with defaults:**
```bash
java -jar target/sqlexpr-load-congocc-1.0.0.jar
```
This loads `complex_expressions-limited.json` from the classpath and runs 1 iteration.

**Run with custom input and iterations:**
```bash
java -jar target/sqlexpr-load-congocc-1.0.0.jar --input data/my-expressions.json --iterations 100
```

## Command Line Options

```
--input FILE       Path to expressions JSON file
                   - Supports absolute paths: /home/user/data/expressions.json
                   - Supports relative paths: file.json (resolved to src/main/resources/file.json)
                   - Default: complex_expressions-limited.json (from classpath)

--iterations N     Number of test iterations (default: 1, minimum: 1)
```

**Examples:**

```bash
# Use default file, 10 iterations
java -jar target/sqlexpr-load-congocc-1.0.0.jar --iterations 10

# Use alternate classpath resource file
java -jar target/sqlexpr-load-congocc-1.0.0.jar --input complex_expressions.json

# Use file from filesystem with relative path
java -jar target/sqlexpr-load-congocc-1.0.0.jar --input complex_expressions-limited.json

# Use file from filesystem with absolute path
java -jar target/sqlexpr-load-congocc-1.0.0.jar --input /path/to/custom-expressions.json --iterations 50

# Arguments can be in any order
java -jar target/sqlexpr-load-congocc-1.0.0.jar --iterations 5 --input data/test.json
```

## Input File Format

JSON array of expression objects, where each object contains:
- `expr`: SQL boolean expression string
- `value_map`: Map of variable names to their values

**Example:**
```json
[
  {
    "expr": "age > 18 AND status = 'active'",
    "value_map": {
      "age": 25,
      "status": "active"
    }
  },
  {
    "expr": "price BETWEEN 100 AND 500 OR category = 'premium'",
    "value_map": {
      "price": 350,
      "category": "standard"
    }
  }
]
```

## Output Reports

The program generates markdown reports in the `output/` directory:

### test_timings.md

Performance statistics organized by expression complexity class:
- **Input File**: Absolute path of the input file used
- **Overall Statistics**: Timestamps, iteration count, total expressions, execution time, failures
- **Per-Complexity-Class Statistics**: Min/max/average/std deviation timing metrics

Complexity is calculated as the count of AND/OR operators in the expression, excluding AND in BETWEEN clauses.

### failed_tests.md

Created only if evaluation failures occur. Contains:
- Failed expression text
- Variable value map that was used
- Error message details

## Requirements

- **Build**: Java 21+, Maven 3.9.9+
- **Runtime**: Java 17+

## Development

For detailed project architecture and development guidance, see [CLAUDE.md](CLAUDE.md).

**Compile:**
```bash
mvn compile
```

**Run tests:**
```bash
mvn test
```

**Run from Maven:**
```bash
mvn exec:java -Dexec.mainClass="net.magneticpotato.Main"
mvn exec:java -Dexec.args="--input file.json --iterations 10"
```

## Project Structure

```
src/main/java/net/magneticpotato/
├── Main.java                   # Entry point, CLI parsing
├── model/                      # Data models (records)
│   ├── ClassStatistics.java
│   ├── ClassTimings.java
│   ├── ExpressionData.java
│   ├── FailedEvaluation.java
│   ├── LoadTestResult.java
│   └── OverallStatistics.java
└── service/                    # Business logic
    ├── ComplexityCalculator.java
    ├── ExpressionLoader.java
    ├── LoadTester.java
    └── ReportGenerator.java
```

## License

See project repository for license information.

## Acknowledgments

Anthopic's Claude Sonnet 4.5 was used to generate most of the code and documentation in this project.  See [docs/command_prompts.md](docs/command_prompts.md) for prompt history.
