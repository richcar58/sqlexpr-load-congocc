# Load and Performance Testing SqlExpr-Rust

The ultimae goal of this project is to generate 10,000 evaluation tests for the *sqlexpr-congocc* project.  The tests will be used to compare the performance of different implementations of the parser/evaluator  that accept the same expression language.  We build the test data in phases so that we can validate results step by step. 

## Load Background Information

claude
/init
brave_web_search sqlexpr-congocc at https://github.com/richcar58/sqlexpr-congocc

## Generate Load Test Program

Boolean expression evaluation is implemented in the sqlexpr-congocc project by the *net.magneticpotato.sqlexpr.congocc.SqlExprEvaluator* class.  Specifically, the *SqlExprEvaluator.match(String sqlText, Map<String, Object> properties)* method takes as input a boolean expression, *sqlText*, and map of variable names to concrete values, *properties*.  After parsing the expression string and substituting values for variable names, *match()* returns true, false or an error if evaluation fails.  The following sections describe how we want to load test sqlexpr-congocc's expression evaluation.

### Overview

The Main.java program will first read the JSON file *src/main/resources/complex_expressions.json*, which contains an array of JSON objects.  Each object has an *expr* field than contains a boolean expression string and a *value_map* that contains key/value pairs used to bind variables in the expression.  Each key is a variable name and the value is the variable's value.  The program will time calls to *SqlExprEvaluator.match(String sqlText, Map<String, Object> properties)* from the sqlexpr-congocc library, calculate runtime statistics and output the results. 

Example calls to *SqlExprEvaluator.match(String sqlText, Map<String, Object> properties)* can be found in sqlexpr-congocc's test program, *net.magneticpotato.sqlexpr.congocc.SqlExprEvaluatorTest*.

#### Definitions

The following definitions pertain to complex boolean expressions:

1. **Expression Complexity** - The complexity of a boolean expression is the number of logical OR (disjuction) and AND (conjunction) operators it contains.
2. **Complexity Class** - A complexity class is the set of expressions with the same expression complexity.

### Command Line Parameters

The main program will optionally take a single integer parameter, *--iterations*, which specifies how many times each complex expression is run.  If unspecified, the *--iterations* defaults to 1.  Otherwise, the specified value must be an integer 1 or greater.

### Load Testing

The program executes the following steps:
1. Read in the input file *src/main/resources/complex_expressions.json*.
2. Create a hash table, *classTimings*, to record the time it takes to evaluates all expressions in each complexity class.  
    1. The table's keys are integers that represent the complexity classes.
    2. The table's values are objects that record timings and counts for expressions in the class:
        1. Accumulated execution time for all expressions in this class.
        2. Minimum and maximum execution times for expressions in this class.
        3. The total number of evaluations in this class.
        4. The number of unique expressions evaluated in this class.
    3. All times are in milliseconds and represented as floating point numbers.  
    4. Each time a new complexity class is encountered, a new hash key is inserted into *classTimings* and its value object is initialized appropriately. 
3. For each JSON object in the top-level array of the input file, execute these steps:
    1. Determine the complexity class of the expression assigned to the *expr* field.
        1. Be careful not to include the "AND" that appears in BETWEEN clauses as a logical AND.  For example, in the relational clause "f92 NOT BETWEEN 20 AND 30", he "AND" between the lower value (20) and upper value (30) in this example should not be counted when calculating the complexity class of an expression.  In general, detect when "AND" is part of a BETWEEN clause and don't include it in complexity class calculations.
    2. Execute the following set of steps *--iterations* times:
        1. Time the execution of *SqlExprEvaluator.match(String sqlText, Map<String, Object> properties)* using the object's *expr* and *value_map* fields.
        2. Add the execution time to expression's complexity class bucket in the *classTimings* table.
        3. Maintain the minimum and maximum execution times for the expression's complexity class.
        4. Maintain the total evaluations and unique expressions count.
        5. If an error occurs, save the complete JSON object (*expr* and *value_map*) and the error message returned from evaluate() in an *errorList*.  Do not attempt to evaluate that object again.
4. Once all expressions have been evaluated, calculate the following statistics:
    1. Total number of evaluations executed.
    2. Total time for all evaluations to run in milliseconds.
    3. Total execution time for each complexity class in milliseconds.
    4. The minimum, maximum and average execution times for each complexity class in milliseconds.
    5. The standard deviation for each complexity class.
    6. Number of failed evaluations.
5. Output the statistics to file *output/test_timings.md* in a format that's easily read my humans.
    1. Output a timestamp in both local time and in UTC as the first items in the Overall Statistics section.  The timestamp is taken when processing begins.
    2. Next in the Overall Statistics section, output the line **Iterations**: <--iterations>.
    3. All column widths in the output tables to be wide enough to hold the longest metric string value plus padding.
    4. See https://github.com/richcar58/sqlexpr-load-rust/blob/main/output/test_timings.md for an example of the output formatting that should be replicated as closely as possible.
6. If *errorList* is not empty, print the failed JSON objects with their error messages to *output/failed_tests.md*.

Please create a plan for implementing the load test program.

## Configuring the Test Environment

Maven and Java are available to in bash terminals by exporting the following paths:

    export PATH=~/pkgs/apache-maven-3.9.9/bin:$PATH
    export JAVA_HOME=/home/rich/pkgs/jdk-21.0.5+11
    export PATH=$JAVA_HOME/bin:$PATH

Please run the verification tests that were part of the plan.

## Finalizing Release 1.0.0

Everything looks good for Release 1.0.0!  

Create an MIT license in a file named LICENSE.

Create a README.md file that:
1. Give an overview of what this project does and its purpose.
    1. Reference this project's github repository at https://github.com/richcar58/sqlexpr-load-rust.
    2. Point the user to the input file *resources/complex_expressions.json*.
2. Give directions on how to compile the code.
3. Give directions on how to run the code and supported command line parameters.

Update CLAUDE.MD with the latest design and features descriptions.

Create a RELEASE_NOTES.md file that provides basic information on release 1.0.0.

# User Specified Input File Support

The release 1.0.0 code always reads it input from the *releases/complex_expressions.json* file.  This enhancement allows users to specifiy which input file should be processed on a given run.  Here is what the new support for input file choice entails:

1. A new, optional, command line argument, *--input*, will specify which expression file will be processed.
2. If *--input* is not specified, the default value  *complex_expressions-limited.json* is used.
    1. If the *--input* value is an absolute path, then that path will be used as is
    2. If the *--input* value is a relative path, it is relative to the *resources* directory.  This implies that the default input file is found at *resources/complex_expressions-limited.json*.
3. The *output/test_timings.md* result file will now include the absolute path of the input file in its *Overall Statistics* section.
4. The changes will be validated by running the load test without specifying a *--input* argument.
5. The README.md and CLAUDE.md files will be updated to reflect this new enhancement.

Please generate a plan implement the above code, validation and documentation requirements.  Don't make any changes until the plan is reviewed and approved.

Please generate a RELEASE_NOTES.md file that describes the main features of the 1.0.0 release.
