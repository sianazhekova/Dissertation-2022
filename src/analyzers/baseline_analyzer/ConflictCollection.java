package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConflictCollection {

    /*
    * This collection object will be added to the ConflictTable Class.
    * Encapsulates the aggregation of conflict objects in an array-like structure.
    */

    LinkedHashSet<InstructionLevelConflict> conflictsCollection;

    public ConflictCollection() {
        conflictsCollection = new LinkedHashSet<>();
    }

    public ConflictCollection(List<InstructionLevelConflict> existingCollection) {
        conflictsCollection.addAll(existingCollection);
    }

    // TODO: Check if hashing an InstructionLevelConflict object is unique even if it has the same fields as an exisitng one in the set
    public boolean canPartiallyCollect(@NotNull InstructionLevelConflict inputConflictObject) {
        for (InstructionLevelConflict currConflictObject : conflictsCollection) {
            if (currConflictObject.isAdditive(inputConflictObject)) {
                currConflictObject.addCountsFrom(inputConflictObject);
                return true;
            }
        }
        return false;
    }

    public void summariseInstructions(@NotNull LinkedHashSet<InstructionLevelConflict> summaryList) {
        for (InstructionLevelConflict currConflict :  summaryList) {
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
        for (InstructionLevelConflict currConflict : conflictsCollection) {
            if (counter != conflictsCollection.size() - 1)
                sb.append(currConflict.printToString() + "\n");
            counter++;
        }
        return sb.toString();
    }
}
