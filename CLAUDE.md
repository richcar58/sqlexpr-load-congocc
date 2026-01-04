# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a load testing and performance evaluation program for the sqlexpr-congocc library. It reads SQL boolean expressions from JSON, evaluates them using the SqlExprEvaluator API, tracks performance statistics by complexity class, and generates formatted markdown reports.

**Group ID:** net.magneticpotato
**Artifact ID:** sqlexpr-load-congocc
**Version:** 1.0.0

**Purpose**: Benchmark the sqlexpr-congocc expression evaluator and generate performance statistics for comparison with other implementations (e.g., Rust version at https://github.com/richcar58/sqlexpr-load-rust).

**Key Features**:
- Loads 10,000+ test expressions from JSON file (10.6MB)
- Calculates expression complexity (count of AND/OR operators, excluding BETWEEN clauses)
- Times each evaluation with nanosecond precision
- Generates detailed statistics per complexity class (min, max, avg, std deviation)
- Outputs markdown reports for easy analysis

## Build Commands

This project uses Maven 3.9.9+ for build management and requires Java 21+ for compilation (targets Java 17+ runtime).

**Compile:**
```bash
mvn compile
```

**Clean and build:**
```bash
mvn clean compile
```

**Package:**
```bash
mvn package
```

**Run tests:**
```bash
mvn test
```

**Run a single test:**
```bash
mvn test -Dtest=TestClassName
```

**Run the main class:**
```bash
mvn exec:java -Dexec.mainClass="net.magneticpotato.Main"
```

## Architecture

### Package Structure

```
src/main/java/net/magneticpotato/
├── Main.java                          # Entry point, CLI parsing
├── model/                             # Data models
│   ├── ClassStatistics.java          # Computed stats per complexity class
│   ├── ClassTimings.java             # Timing tracker per complexity class
│   ├── ExpressionData.java           # JSON deserialization record
│   ├── FailedEvaluation.java         # Failed test details
│   ├── LoadTestResult.java           # Result container
│   └── OverallStatistics.java        # Overall test statistics
└── service/                           # Business logic
    ├── ComplexityCalculator.java     # Expression complexity calculation
    ├── ExpressionLoader.java         # JSON loading with streaming
    ├── LoadTester.java               # Test orchestration
    └── ReportGenerator.java          # Markdown report generation
```

### Core Components

**ComplexityCalculator**: Calculates expression complexity by counting AND/OR operators while excluding AND in BETWEEN clauses (e.g., "x BETWEEN 1 AND 10" has complexity 0). Uses string processing with placeholder replacement.

**ExpressionLoader**: Uses Jackson streaming API to efficiently load large JSON files without loading entire file into memory. Parses 10.6MB file with 332k lines.

**LoadTester**: Orchestrates testing by:
1. Pre-calculating complexity for all expressions (cached)
2. Running N iterations of evaluations
3. Tracking failed expressions (skip in remaining iterations)
4. Recording timing with nanosecond precision
5. Computing statistics (min, max, avg, std deviation with Bessel's correction)

**ReportGenerator**: Generates two markdown reports:
- `output/test_timings.md`: Performance statistics by complexity class
- `output/failed_tests.md`: Details of failed evaluations (if any)

### Data Flow

1. Parse command line arguments (`--input`, `--iterations`)
2. Load expressions from specified file or default classpath resource
   - Resolve absolute path for reporting
3. Calculate complexity class for each expression
4. For each iteration:
   - Time evaluation of each expression using `SqlExprEvaluator.match()`
   - Track timing in appropriate complexity class bucket
   - Skip expressions that previously failed
5. Compute statistics per complexity class
6. Generate formatted markdown reports with input file path

## Dependencies

**sqlexpr-congocc (1.0.0)**: The SQL expression parser/evaluator library being tested
- Group ID: `net.magneticpotato`
- Main API: `SqlExprEvaluator.match(String sqlText, Map<String, Object> properties)`
- Repository: https://github.com/richcar58/sqlexpr-congocc
- Purpose: Parses and evaluates SQL-like boolean expressions without requiring a database
- Built with CongoCC parser generator

**Jackson Databind (2.18.0)**: JSON parsing with streaming support
- Used for efficiently loading large JSON files
- Enables processing 10.6MB file without loading entire content into memory

## Usage

**Run with default 1 iteration and default input file:**
```bash
mvn exec:java
```

**Run with custom iterations:**
```bash
mvn exec:java -Dexec.args="--iterations 100"
```

**Run with custom input file:**
```bash
mvn exec:java -Dexec.args="--input complex_expressions.json"
```

**Run with both custom input and iterations:**
```bash
mvn exec:java -Dexec.args="--input /path/to/expressions.json --iterations 100"
```

**Build executable JAR and run:**
```bash
mvn package
java -jar target/sqlexpr-load-congocc-1.0.0.jar --input custom.json --iterations 100
```

**Command Line Arguments:**
- `--input FILE`: Path to JSON expressions file (absolute or relative to `src/main/resources/`)
  - Default: `complex_expressions-limited.json` from classpath
  - Supports absolute paths: `/home/user/data/expressions.json`
  - Supports relative paths: `file.json` (resolved to `src/main/resources/file.json`)
- `--iterations N`: Number of test iterations (default: 1, must be >= 1)

**Output Files:**
- `output/test_timings.md`: Performance statistics by complexity class (includes input file path)
- `output/failed_tests.md`: Failed evaluation details (created only if failures occur)

## Development Notes

- Java 21+ required for compilation (configured in pom.xml)
- Target runtime: Java 21+
- Maven 3.9.9+ required
- Build artifacts are excluded via .gitignore (target/ directory, .class and .jar files)
- Uses Java records for immutable data models
- Standard deviation calculated with Bessel's correction (n-1 denominator)
- Timing precision: nanosecond-level using System.nanoTime()
