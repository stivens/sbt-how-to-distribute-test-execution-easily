# [How to] SBT - Distribute Test Execution Easily

This project demonstrates how to distribute test execution across multiple processes using SBT's built-in capabilities. Instead of running all tests sequentially, you can split your test suite into groups and run them in parallel, significantly reducing overall test execution time.

## The Problem

When you have a large test suite with many time-consuming tests, running them sequentially can take a very long time. For example, if you have 10 tests that each take 1 minute to run, the total execution time would be 10 minutes when run sequentially.

## The Solution

This project shows how to create a custom SBT task called `parTestGroup` that:

1. **Divides tests into groups** - Splits all available tests into a specified number of groups
2. **Runs specific groups** - Allows you to run just one group of tests at a time
3. **Enables parallel execution** - Multiple machines can run different groups simultaneously

## How It Works

The core functionality is implemented in `build.sbt` as a custom input task:

```scala
lazy val parTestGroup = inputKey[Unit]("Runs a single test group")
parTestGroup := (Def.inputTaskDyn {
  val List(groupId, numberOfGroups) = complete.DefaultParsers
    .spaceDelimited("<arg>")
    .parsed
    .map(_.toInt)

  val allTests = (Test / definedTests).value

  val numberOfTests = allTests.size
  val numberOfTestsPerGroup =
    if (numberOfTests % numberOfGroups == 0) {
      numberOfTests / numberOfGroups
    } else { (numberOfTests / numberOfGroups) + 1 }

  val groups = allTests.grouped(numberOfTestsPerGroup).toArray

  val groupToRun     = groups(groupId - 1)
  val argForTestOnly = " " + groupToRun.map(_.name).mkString(" ")

  streams.value.log.info(s"Running testOnly:$argForTestOnly")

  Def.taskDyn {
    (Test / testOnly).toTask(argForTestOnly)
  }
}).evaluated
```

## Usage

### Basic Usage

To run tests in groups, use the `parTestGroup` task with two parameters:

```bash
sbt "parTestGroup <groupId> <numGroups>"
```

- `groupId`: Which group to run (1-based index)
- `numGroups`: Total number of groups to divide tests into

### Examples

**Example 1: Split tests into 3 groups and run the first group**
```bash
sbt "parTestGroup 1 3"
```

```sbt
sbt:root> parTestGroup 1 3
[info] Running testOnly: io.github.stivens.example.suites.TestSuite5 io.github.stivens.example.suites.TestSuite7 io.github.stivens.example.suites.TestSuite2 io.github.stivens.example.suites.TestSuite6
```

**Example 2: Split tests into 3 groups and run the third group**
```bash
sbt "parTestGroup 3 3"
```

```sbt
sbt:root> parTestGroup 3 3
[info] Running testOnly: io.github.stivens.example.suites.TestSuite8 io.github.stivens.example.suites.TestSuite3
```

### Parallel Execution in CI/CD (Primary Use Case)

The main power of this approach is demonstrated in the GitHub Actions workflows that run test groups on **separate machines** simultaneously:

#### Multi-Machine Workflow (`.github/workflows/ci-run-tests-on-multiple-machines.yml`)

```yaml
jobs:
  compilation:
    name: Compile the project
    runs-on: ubuntu-latest
    # ... compilation steps

  unit-tests-group-1:
    name: Run unit tests (1 of 10)
    needs: [compilation]
    uses: ./.github/workflows/run-unit-tests-group.yml
    with:
      group_id: 1
      num_groups: 10

  unit-tests-group-2:
    name: Run unit tests (2 of 10)
    needs: [compilation]
    uses: ./.github/workflows/run-unit-tests-group.yml
    with:
      group_id: 2
      num_groups: 10

  # ... groups 3-10 running in parallel

  aggregate-all:
    name: compile and test the project
    needs: [compilation, unit-tests-group-1, ..., unit-tests-group-10]
    # Aggregates results from all groups
```

#### Reusable Test Group Workflow (`.github/workflows/run-unit-tests-group.yml`)

```yaml
on:
  workflow_call:
    inputs:
      group_id:
        required: true
        type: string
      num_groups:
        required: true
        type: string

jobs:
  run_unit_tests_group:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - uses: ./.github/actions/setup-scala
    - uses: ./.github/actions/restore-compilation-cache
    - name: Unit tests
      run: sbt 'parTestGroup ${{ inputs.group_id }} ${{ inputs.num_groups }}'
```

#### Performance Comparison

- **Sequential CI** (`.github/workflows/ci-run-all-at-once.yml`): ~5-10 minutes on 1 runner
- **Parallel CI** (`.github/workflows/ci-run-tests-on-multiple-machines.yml`): ~1 minute on 10 runners


## Demo Test Suite

This project includes 14 test suites (`TestSuite1` through `TestSuite14`), each containing a test that sleeps for 1 minute to simulate long-running tests.

### Sequential vs Parallel Execution Time

- **Sequential CI**: ~7-10 minutes (10 tests × 1 minute each) on 1 GitHub runner
- **Parallel CI (5 groups)**: ~3 minutes (3 tests per group × 1 minute each) on 5 GitHub runners
  
*Note: Each test in this demo sleeps for 1 minute to simulate long-running integration tests*

Parallel execution:

![Demo - parallel execution](screenshots/ci-run-tests-on-multiple-machines.png "Parallel exeuction in action")

Single-machine execution:

![Demo - sequential execution](screenshots/ci-run-all-at-once.png "Sequential execution for reference")


## Technical Details

### GitHub Actions Infrastructure

The project includes sophisticated CI/CD workflows:

- **Custom Actions**:
  - `.github/actions/setup-scala/` - JDK 21 and SBT setup with caching
  - `.github/actions/compile/` - Compilation with intelligent caching
  - `.github/actions/restore-compilation-cache/` - Cache restoration for faster builds

- **Workflows**:
  - `ci-run-all-at-once.yml` - Traditional sequential test execution (baseline)
  - `ci-run-tests-on-multiple-machines.yml` - Parallel execution across 5 runners
  - `run-unit-tests-group.yml` - Reusable workflow for individual test groups

- **Features**:
  - Compilation artifact caching between runners
  - Result aggregation with proper failure handling
  - Optimized for GitHub's hosted runners

## Benefits

1. **Dramatically Faster CI/CD Pipelines** - by distributing across multiple GitHub runners
2. **Scalable Architecture** - Easy to adjust the number of groups based on test suite size and available runners
3. **Improved Developer Experience** - Faster feedback loops on pull requests and commits

## Use Cases

### Primary: CI/CD Pipeline Optimization
- **Large Test Suites** - Projects with hundreds or thousands of tests requiring long execution times
- **Integration Tests** - Long-running tests (database, API, end-to-end) that can be parallelized
- **High-Frequency Deployments** - Teams needing fast feedback on multiple daily deployments


## Getting Started

### Try the Demo

1. **Clone this repository**
   ```bash
   git clone git@github.com:stivens/sbt-how-to-distribute-test-execution-easily.git
   cd sbt-how-to-distribute-test-execution-easily
   ```

2. **Run a single test group locally**
   ```bash
   sbt "parTestGroup 1 2"
   ```

### Adapt to Your Project

1. **Copy the SBT task** - Add the `parTestGroup` task definition to your `build.sbt`
2. **Set up GitHub Actions** - Copy and adapt the workflows from `.github/workflows/`
3. **Customize group count** - Adjust `num_groups` based on your test suite size and available runners
4. **Configure caching** - Adapt the compilation caching actions for your project structure

