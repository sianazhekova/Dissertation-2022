package analyzers.sd3_analyzer;

import org.jetbrains.annotations.NotNull;

/* An enumeration class representing (and enumerating) the possible states of the Finite State Machine for Stride Detection.
* */
public enum StrideDetectionState {
    START(0),
    FIRST_OBSERVED(1),
    STRIDE_LEARNED(2),
    WEAK_STRIDE(3),
    STRONG_STRIDE(4)
    ;

    private int stateID;

    StrideDetectionState(int i) { this.stateID = i; }

    StrideDetectionState() { this.stateID = 0; }

    public int getStateID() { return stateID; }

    public static String getStringCollectionAccess(@NotNull StrideDetectionState detectionState) { return detectionState.toString(); }
}
