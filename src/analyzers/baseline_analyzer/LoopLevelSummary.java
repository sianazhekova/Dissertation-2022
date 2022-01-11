package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

public class LoopLevelSummary {

    /*
    * A class summarising the following profiling statistics at the loop level (that have been aggregated over the iterations of all loop instances) :
    * The total number of iterations of the loop instances encountered, the total number of detected data dependencies for the loop instances,
    * the aggregate number of iterations in which data conflicts have been detected, and a collection of all the data conflicts that have been
    * detected throughout the iterations of the loop instance.
    * */

    private long totalNumIterations;
    private long iterationsWithDependencies;

    private long totalNumInstances;
    private long instancesWithConflicts;

    private long totalNumDataConflicts;
    private ConflictCollection conflictCollection;

    LoopLevelSummary() {
        totalNumIterations = 0;
        iterationsWithDependencies = 0;

        totalNumInstances = 0;
        instancesWithConflicts = 0;

        totalNumDataConflicts = 0;
        conflictCollection = new ConflictCollection();
    }

    public void addLoopInstanceConflicts(@NotNull LoopInstanceLevelSummary instanceSummary) {
        totalNumIterations += instanceSummary.getTotalCountIterations();
        iterationsWithDependencies += instanceSummary.getCountConflictIterations();

        totalNumInstances++;
        instancesWithConflicts += instanceSummary.getTotalCountDataDependencies() > 0 ? 1 : 0;

        totalNumDataConflicts += instanceSummary.getTotalCountDataDependencies();
        conflictCollection.summariseSummaryCollection(instanceSummary.getCollectionOfConflicts());
    }

    public String printToString() {
        return String.format(" | Total Number of Loops' Iterations : %d |\n" +
                        " | Total Number of Iterations with Conflicts : %d |\n " +
                        " | Total Number of Loop Instances : %d |\n" +
                        " | Total Number of Loop Instances with Conflicts : %d |\n" +
                        " | Total Number of Conflicts : %d |\n " +
                        " | Detected Conflict Statistics | : [ %s ]\n",
                totalNumIterations,
                iterationsWithDependencies,
                totalNumInstances,
                instancesWithConflicts,
                totalNumDataConflicts,
                conflictCollection.printToString()
        );
    }

    public long getTotalNumIterations() {
        return totalNumIterations;
    }

    public long getIterationsWithDependencies() {
        return iterationsWithDependencies;
    }

    public long getTotalNumInstances() {
        return totalNumInstances;
    }

    public long getInstancesWithConflicts() {
        return instancesWithConflicts;
    }

    public long getTotalNumDataConflicts() {
        return totalNumDataConflicts;
    }

    public ConflictCollection getConflictCollection() {
        return conflictCollection;
    }
}
