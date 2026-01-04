# Release Notes

## Version 1.0.0 - January 3, 2026

### Overview

This is the initial release of sqlexpr-load-congocc, a load testing and performance evaluation program for the [sqlexpr-congocc](https://github.com/richcar58/sqlexpr-congocc) SQL expression evaluator library.  The code repository is [sqlexpr-load-congocc](https://github.com/richcar58/sqlexpr-load-rust).

### Key Features

#### Expression Loading and Evaluation

- **Flexible Input Sources**: Load test expressions from JSON files via multiple methods:
  - Default classpath resource (`complex_expressions-limited.json`)
  - Custom files specified via `--input` argument
  - Support for both absolute and relative file paths
  - Relative paths resolved relative to `src/main/resources/` directory

- **Large File Support**: Efficiently processes large JSON files (10+ MB, 10,000+ expressions) using Jackson streaming API to minimize memory footprint

- **Robust Error Handling**: Clear error messages for file not found, invalid JSON, and permission issues

#### Performance Analysis

- **Expression Complexity Classification**: Automatically calculates complexity based on logical operator count
  - Counts AND and OR operators in expressions
  - Intelligently excludes AND operators within BETWEEN clauses
  - Groups expressions by complexity class for detailed analysis

- **High-Precision Timing**: Nanosecond-level timing precision using `System.nanoTime()` for accurate performance measurement

- **Multi-Iteration Testing**: Support for running multiple test iterations to gather statistical significance
  - Configurable via `--iterations` argument
  - Aggregates results across all iterations

- **Failure Tracking**: Automatically identifies and skips failed expressions in subsequent iterations to focus on valid test cases

#### Statistical Reporting

- **Comprehensive Statistics**: Generates detailed performance metrics per complexity class:
  - Minimum execution time
  - Maximum execution time
  - Average execution time
  - Standard deviation (calculated with Bessel's correction)
  - Total execution time
  - Total evaluations count
  - Unique expressions count

- **Overall Summary**: Provides test-wide statistics including:
  - Input file path (absolute, resolved path)
  - Test timestamps (local and UTC)
  - Total unique expressions tested
  - Total evaluations executed
  - Total execution time
  - Failed evaluation count

#### Report Generation

- **Markdown Output**: Generates formatted markdown reports for easy viewing and sharing
  - `output/test_timings.md`: Performance statistics by complexity class
  - `output/failed_tests.md`: Detailed failure information (created only when failures occur)

- **Failure Details**: Failed evaluation reports include:
  - Complete SQL expression text
  - Variable value map used in evaluation
  - Full error message and exception type

#### Command Line Interface

- **Intuitive Arguments**: Simple, flexible command-line interface
  - `--input FILE`: Specify input JSON file (optional)
  - `--iterations N`: Set number of test iterations (optional, default: 1)
  - Arguments accepted in any order

- **Smart Defaults**: Runs out-of-the-box with sensible defaults
  - Default input: `complex_expressions-limited.json` from classpath
  - Default iterations: 1

- **Helpful Error Messages**: Clear usage information and error messages guide users

#### Technical Features

- **Java 21+ Implementation**: Modern Java with records for immutable data models
- **Java 17+ Runtime**: Compatible with Java 17 and later for broader deployment
- **Maven Build System**: Standard Maven project structure with clean/compile/package/test targets
- **Executable JAR**: Builds standalone executable JAR with all dependencies included
- **Streaming JSON Parser**: Memory-efficient processing of large JSON files
- **CongoCC Integration**: Tests the sqlexpr-congocc library built with CongoCC parser generator

### Usage Examples

#### Basic Usage

```bash
# Run with defaults (1 iteration, default file)
java -jar sqlexpr-load-congocc-1.0.0.jar

# Run with 100 iterations
java -jar sqlexpr-load-congocc-1.0.0.jar --iterations 100
```

#### Custom Input Files

```bash
# Use alternate resource file
java -jar sqlexpr-load-congocc-1.0.0.jar --input complex_expressions.json

# Use external file with absolute path
java -jar sqlexpr-load-congocc-1.0.0.jar --input /data/custom-expressions.json

# Use file with relative path (resolved to src/main/resources/)
java -jar sqlexpr-load-congocc-1.0.0.jar --input my-test-data.json
```

#### Combined Arguments

```bash
# Custom input and iterations (arguments in any order)
java -jar sqlexpr-load-congocc-1.0.0.jar --input data.json --iterations 50
java -jar sqlexpr-load-congocc-1.0.0.jar --iterations 50 --input data.json
```

### Requirements

- **Build**: Java 21+, Maven 3.9.9+
- **Runtime**: Java 21+

### Dependencies

- **sqlexpr-congocc 1.0.0**: SQL expression parser/evaluator library (tested component)
- **Jackson Databind 2.18.0**: JSON parsing with streaming support
- **JUnit Jupiter 5.10.1**: Testing framework (test scope)

### Project Structure

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

### Documentation

- **README.md**: User-facing documentation with quick start guide
- **CLAUDE.md**: Development guidance for AI assistants and developers
- **RELEASE_NOTES.md**: This file

### Performance Benchmarking

This tool enables performance comparison across different SQL expression evaluator implementations:

- Baseline performance metrics for sqlexpr-congocc (Java/CongoCC)
- Comparable with other implementations (e.g., [sqlexpr-load-rust](https://github.com/richcar58/sqlexpr-load-rust))
- Reproducible test methodology for consistent benchmarking
- See [output/test_timings.md](output/test_timings.md) for an example output file

### Known Limitations

- Relative path resolution to `src/main/resources/` only works when running from source code; when packaged as JAR, external files must use absolute paths
- Test expressions file must be valid JSON array format
- No support for test expression generation (requires pre-existing JSON file, see [sqlexpr-gen](https://github.com/richcar58/sqlexpr-gen))
- Currently, `!=` is not recognized as an alternate form of the NOT EQUALS operator, `<>`

### Future Considerations

Potential enhancements for future releases may include:

- Support for additional input formats (CSV, XML)
- Configurable output formats (JSON, CSV, HTML)
- Real-time progress reporting for long-running tests
- Parallel expression evaluation for faster testing
- Expression generation utilities
- Performance regression detection
- Comparison with baseline results

### Acknowledgments

Most of the code and documentation in this project was generated with assistance from Anthropic's Claude Sonnet 4.5. See [docs/command_prompts.md](docs/command_prompts.md) for prompt history.

### License

See project repository for MIT license information.
