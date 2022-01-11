package analyzers.baseline_analyzer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LoopInstanceLevelSummary {

    /*
     *  A summary of the statistics of the detected conflicts per given Loop Instance:
     *  It contains information about the ID, total number of iterations for a given loop instance,
     *  its number of loop iterations with at least one conflict detected in each,
     *  and a collection of conflicts (encapsulated in a custom class type) detected within the loop.
     */

    private final long loopID;
    private long totalCountIterations;
    private long countConflictIterations;
    private long totalCountDataDependencies;
    private ConflictCollection collectionOfConflicts;

    public LoopInstanceLevelSummary(long newLoopID) {
        loopID = newLoopID;
        totalCountIterations = 0;
        countConflictIterations = 0;
        totalCountDataDependencies = 0;
        collectionOfConflicts = new ConflictCollection();
    }

    public void addLoopIterationConflicts(long numConflicts, @NotNull LinkedHashSet<InstructionLevelConflict> conflictList) {
        this.collectionOfConflicts.summariseInstructions(conflictList);
        incrementIterationState(numConflicts);
    }

    public void incrementIterationState(long numIterationConflicts) {
        totalCountIterations++;
        countConflictIterations = (numIterationConflicts > 0) ? 1 : 0;
        totalCountDataDependencies += numIterationConflicts;
    }

    public String printToString() {
        return String.format(" | LoopID : %d |\n" +
                " | Total Number of Loop Iterations : %d |\n" +
                " | Total Number of Iterations with Conflicts : %d |\n " +
                " | Total Number of Conflicts : %d |\n " +
                " | Detected Conflict Statistics | : [ %s ]\n",
                loopID,
                totalCountIterations,
                countConflictIterations,
                totalCountDataDependencies,
                collectionOfConflicts.printToString()
        );
    }

    public long getCountConflictIterations() {
        return countConflictIterations;
    }

    public long getTotalCountDataDependencies() {
        return totalCountDataDependencies;
    }

    public long getTotalCountIterations() {
        return totalCountIterations;
    }

    public long getLoopID() {
        return loopID;
    }

    public ConflictCollection getCollectionOfConflicts() {
        return collectionOfConflicts;
    }
}
