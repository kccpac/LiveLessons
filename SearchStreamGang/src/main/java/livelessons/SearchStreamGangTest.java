package livelessons;

import livelessons.streamgangs.*;
import livelessons.utils.Options;
import livelessons.utils.TestDataFactory;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;

/**
 * This test driver showcases how implementation strategies customize
 * the SearchStreamGang framework with different Java 8 mechanisms to
 * implement an "embarrassingly parallel" program that searches for
 * phases in a list of input strings.
 */
public class SearchStreamGangTest {
    /**
     * Enumerate all the implementation strategies to run.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM,
        SEQUENTIAL_LOOPS,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_PHASES,
        PARALLEL_STREAMS,
        PARALLEL_SPLITERATOR,
        COMPLETABLE_FUTURES_PHASES,
        COMPLETABLE_FUTURES_INPUTS,
        RXJAVA_PHASES,
        RXJAVA_INPUTS
    }

    /**
     * Factory method that creates the desired type of
     * SearchStreamGang subclass implementation strategy.
     */
    private static SearchStreamGang makeSearchStreamGang(List<String> phaseList,
                                                         List<List<CharSequence>> inputData,
                                                         TestsToRun choice) {
        switch (choice) {
        case SEQUENTIAL_LOOPS:
            return new SearchWithSequentialLoops(phaseList,
                        inputData);
        case SEQUENTIAL_STREAM:
            return new SearchWithSequentialStreams(phaseList,
                                                  inputData);
        case PARALLEL_SPLITERATOR:
            return new SearchWithParallelSpliterator(phaseList,
                                                     inputData);
        case PARALLEL_STREAM_INPUTS:
            return new SearchWithParallelStreamInputs(phaseList,
                                                      inputData);
        case PARALLEL_STREAM_PHASES:
            return new SearchWithParallelStreamPhrases(phaseList,
                                                     inputData);
        case PARALLEL_STREAMS:
            return new SearchWithParallelStreams(phaseList,
                                                 inputData);
        case COMPLETABLE_FUTURES_PHASES:
            return new SearchWithCompletableFuturesPhrases(phaseList,
                                                         inputData);
        case COMPLETABLE_FUTURES_INPUTS:
            return new SearchWithCompletableFuturesInputs(phaseList,
                                                          inputData);
        case RXJAVA_INPUTS:
            return new SearchWithRxJavaInputs(phaseList, inputData);
        case RXJAVA_PHASES:
            return new SearchWithRxJavaPhrases(phaseList, inputData);
        }
        return null;
    }

    /*
     * Input files.
     */
    /**
     * The complete works of William Shakespeare.
     */
    private static String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A list of phrases to search for in the complete works of
     * Shakespeare.
     */
    private static String sPHASE_LIST_FILE =
        "phraseList.txt";

    /**
     * This is the entry point into the test driver program.
     */
    public static void main(String[] args) throws Throwable {
        System.out.println("Starting SearchStreamGangTest");

        // Parse the command-line arguments.
        Options.getInstance().parseArgs(args);

        // All the input is initialized here.
        List<List<CharSequence>> inputData = 
            new ArrayList<List<CharSequence>>() {
                { // Create a list of input from the complete works of
                  // William Shakespeare.
                  add(TestDataFactory
                      // Split input by input separator from Options singleton.
                      .getSharedInput(sSHAKESPEARE_DATA_FILE,
                                      Options.getInstance().getInputSeparator()));
                }
            };

        // Get the list of input phases to find.
        List<String> phaseList = 
            TestDataFactory.getPhraseList(sPHASE_LIST_FILE);

        // Create/run StreamGangs to search for the phases to find.
        runTests(phaseList,
                 inputData);

        System.out.println("Ending SearchStreamGangTest");
    }

    /**
     * Create/run appropriate type of StreamGang to search for phases.
     */
    private static void runTests(List<String> phaseList,
                                 List<List<CharSequence>> inputData) {
        // Run all the SearchStreamGang tests.
        for (TestsToRun test : TestsToRun.values()) {
            System.out.println("Starting " + test);

            // Use the factory method to make the appropriate
            // SearchStreamGang.
            SearchStreamGang streamGang =
                makeSearchStreamGang(phaseList,
                                     inputData,
                                     test);

            // Execute the test.
            assert streamGang != null;
            streamGang.run();

            // Store the execution times into the results map.
            mResultsMap.put(test.toString(),
                            streamGang.executionTimes());

            // Run the garbage collector to free up memory and
            // minimize timing perturbations on each test.
            System.gc();

            System.out.println("Ending " + test);
        }

        // Sort and display all the timing results.
        printTimingResults(mResultsMap);
    }

    /**
     * Print out all the timing results for all the test runs in order
     * from fastest to slowest.
     */
    private static void printTimingResults(Map<String, List<Long>> resultsMap) {
        // Determine how many runs of the tests took place.
        int numberOfRuns =
            resultsMap.entrySet().iterator().next().getValue().size();

        // Iterate through the results of each of the test runs.
        for (int i = 0;
             i < numberOfRuns;
             i++) {
            final int runNumber = i;
            System.out.println("\nPrinting "
                               + resultsMap.entrySet().size()
                               + " results for input file "
                               + (runNumber + 1)
                               + " from fastest to slowest");

            // Print out the contents of the resultsMap in sorted
            // order.
            resultsMap
                // Get the entrySet for the resultsMap.
                .entrySet()

                // Convert the entrySet into a stream.
                .stream()

                // Create a SimpleImmutableEntry containing the timing
                // results (value) followed by the test name (key).
                .map(entry
                     -> new SimpleImmutableEntry<>
                        (entry.getValue().get(runNumber),
                         entry.getKey()))

                // Sort the stream by the timing results (key).
                .sorted(Comparator.comparing(SimpleImmutableEntry::getKey))

                // Print all the entries in the sorted stream.
                .forEach(entry
                         -> System.out.println(""
                                               + entry.getValue()
                                               + " executed in "
                                               + entry.getKey()
                                               + " msecs"));
        }
    }

    /**
     * Keep track of which SearchStreamGang performed the best.
     */
    private static Map<String, List<Long>> mResultsMap = new HashMap<>();
}

