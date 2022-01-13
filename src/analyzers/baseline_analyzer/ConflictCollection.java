package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConflictCollection {

    /*
    * This collection object will be added to the ConflictTable Class.
    * Encapsulates the aggregation of conflict objects in an array-like structure.
    */

    LinkedHashSet<PairwiseConflictLevelSummary> conflictsCollection;

    public ConflictCollection() {
        conflictsCollection = new LinkedHashSet<>();
    }

    public ConflictCollection(LinkedHashSet<PairwiseConflictLevelSummary> existingCollection) {
        conflictsCollection.addAll(existingCollection);
    }

    // TODO: Check if hashing an PairwiseConflictLevelSummary object is unique even if it has the same fields as an exisitng one in the set
    public boolean canPartiallyCollect(@NotNull PairwiseConflictLevelSummary inputConflictObject) {
        for (PairwiseConflictLevelSummary currConflictObject : conflictsCollection) {
            if (currConflictObject.isAdditive(inputConflictObject)) {
                currConflictObject.addCountsFrom(inputConflictObject);
                return true;
            }
        }
        return false;
    }

    public void summariseInstructions(@NotNull LinkedHashSet<PairwiseConflictLevelSummary> summaryList) {
        for (PairwiseConflictLevelSummary currConflict :  summaryList) {
            if (!canPartiallyCollect(currConflict)) {
                conflictsCollection.add(currConflict);
            }
        }
    }

    public void summariseSummaryCollection(@NotNull ConflictCollection anotherCollection) {
        summariseInstructions(anotherCollection.conflictsCollection);
    }

    public String printToString() {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        for (PairwiseConflictLevelSummary currConflict : conflictsCollection) {
            if (counter != conflictsCollection.size() - 1)
                sb.append(currConflict.printToString() + "\n");
            counter++;
        }
        return sb.toString();
    }
}
