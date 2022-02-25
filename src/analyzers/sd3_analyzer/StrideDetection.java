package analyzers.sd3_analyzer;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class StrideDetection {

    private StrideDetectionState currentState;
    private BigInteger strideDistance;
    private BigInteger startAddress;
    private BigInteger prevPCAddress;

    private boolean isMonotonic;


    public StrideDetection() {
        strideDistance = BigInteger.ZERO;
        startAddress = BigInteger.ZERO;
        prevPCAddress = BigInteger.ZERO;
        isMonotonic = true;

        currentState = StrideDetectionState.START;
    }

    /* This constructor takes into account whether the stride detection that will be performed by the FSM is for
        monotonic (strictly increasing/decreasing) strides only, or whether it extends to the general case - including "irregular" strides such as [10, 14, 18, 14, 18, 22, 18, 22, 26]  */
    public StrideDetection(boolean monotonicOrGeneral) {
        new StrideDetection();
        isMonotonic = monotonicOrGeneral;
    }

    public StrideDetectionState getCurrentState() { return currentState; }

    /* This method return the stride distance that has been "learned" by the FSM. */
    public BigInteger getStrideDistance() { return strideDistance; }

    /* This method returns the absolute value of the stride distance that has been computed by the FSM. */
    public BigInteger getAbsStrideDistance() { return strideDistance.abs(); }

    public BigInteger getStartAddress() { return startAddress; }

    public BigInteger getPrevPCAddress() { return prevPCAddress; }

    /* Based on the newly added (reference) memory access for a particular PC address and the current state the FSM is in,
        we determine the state transition and update the class members, accordingly. */
    public AccessCollectionType getPointOrStride(BigInteger newAccess) {
        AccessCollectionType accessType;
        updateFSMStateGeneral(newAccess);

        switch(currentState) {
            case START:
            case FIRST_OBSERVED:
            case STRIDE_LEARNED:
                accessType = AccessCollectionType.POINT;
                break;
            case WEAK_STRIDE:
            case STRONG_STRIDE:
                accessType = AccessCollectionType.STRIDE;
                break;
            default:
                accessType = AccessCollectionType.NONE;
        }
        return accessType;
    }

    public boolean isMonotonic() {
        return isMonotonic;
    }

    public String detectGeneralOrMonotonic() {
        return new String(isMonotonic ? "Monotonic Stride Detection" : "General Stride Detection");
    }

    public boolean isAPoint(BigInteger newAccess) { return getPointOrStride(newAccess) == AccessCollectionType.POINT; }

    public boolean isAStride(BigInteger newAccess) { return getPointOrStride(newAccess) == AccessCollectionType.STRIDE; }

    /* Try with both strictly increasing/decreasing sequences per stride as well as non-monotonic ones such as [10, 14, 18, 14, 18, 22, 18, 22, 26].
       TODO: Will evaluate performance of the former against the latter. */

    public int updateFSMStateMonotonic(@NotNull BigInteger newAddress) {
        BigInteger addressDiff = newAddress.subtract(prevPCAddress);
        StrideDetectionState prevState = currentState;

        if (currentState.equals(StrideDetectionState.START)) {
            /* The system is at the Starting State of the Finite State Machine */

            startAddress = newAddress;
            currentState = StrideDetectionState.FIRST_OBSERVED;
        } else if (currentState.equals(StrideDetectionState.FIRST_OBSERVED)) {
            /* The system is at the First Observed State of the Finite State Machine */

            strideDistance = newAddress.subtract(startAddress);
            prevPCAddress = newAddress;
            currentState = StrideDetectionState.STRIDE_LEARNED;
        } else if (currentState.equals(StrideDetectionState.STRIDE_LEARNED)) {
            /* The system is at the Stride Learned State of the Finite State Machine */

            if (addressDiff.equals(strideDistance)) {
                if (!addressDiff.equals(BigInteger.ZERO)) {
                    currentState = StrideDetectionState.WEAK_STRIDE;
                    prevPCAddress = newAddress;
                }
            } else {
                setToFirstObserved(newAddress);
            }
        } else if (currentState.equals(StrideDetectionState.WEAK_STRIDE)) {
            /* The system is at the Weak Stride State of the Finite State Machine */

            if (addressDiff.equals(strideDistance)) {
                currentState = StrideDetectionState.STRONG_STRIDE;
                prevPCAddress = newAddress;
            } else {
                currentState = StrideDetectionState.STRIDE_LEARNED;
            }

        } else if (currentState.equals(StrideDetectionState.STRONG_STRIDE)) {
            /* The system is at the Strong Stride State of the Finite State Machine */

            if (addressDiff.equals(strideDistance)) {
                prevPCAddress = newAddress;

            } else {
                currentState = StrideDetectionState.WEAK_STRIDE;
            }

        }

        return (currentState.getStateID() - prevState.getStateID());
    }

    public int updateFSMStateGeneral(@NotNull BigInteger newAddress) {
        BigInteger addressDiff = newAddress.subtract(startAddress);
        StrideDetectionState prevState = currentState;

        if (currentState.equals(StrideDetectionState.START)) {
            /* The system is at the Starting State of the Finite State Machine */

            startAddress = newAddress;
            currentState = StrideDetectionState.FIRST_OBSERVED;
        } else if (currentState.equals(StrideDetectionState.FIRST_OBSERVED)) {
            /* The system is at the First Observed State of the Finite State Machine */

            prevPCAddress = newAddress;
            strideDistance = newAddress.subtract(startAddress);
            currentState = StrideDetectionState.STRIDE_LEARNED;
        } else if (currentState.equals(StrideDetectionState.STRIDE_LEARNED)) {
            /* The system is at the Stride Learned State of the Finite State Machine */

            if (addressDiff.equals(strideDistance)) {
                if (!addressDiff.equals(BigInteger.ZERO)) {
                    currentState = StrideDetectionState.WEAK_STRIDE;
                    prevPCAddress = newAddress;
                }
            } else {
                setToFirstObserved(newAddress);
            }
        } else if (currentState.equals(StrideDetectionState.WEAK_STRIDE)) {
            /* The system is at the Weak Stride State of the Finite State Machine */

            if (addressDiff.mod(strideDistance).equals(BigInteger.ZERO) && (addressDiff.divide(strideDistance)).compareTo(BigInteger.ZERO) > -1) {
                /* If the current address can be represented as [ startPCAddress + i * strideDistance ] where i is an integer >= 0 */
                currentState = StrideDetectionState.STRONG_STRIDE;
            } else {
                currentState = StrideDetectionState.STRIDE_LEARNED;
            }
            prevPCAddress = newAddress;

        } else if (currentState.equals(StrideDetectionState.STRONG_STRIDE)) {
            /* The system is at the Strong Stride State of the Finite State Machine */

            if (addressDiff.mod(strideDistance).equals(BigInteger.ZERO) && (addressDiff.divide(strideDistance)).compareTo(BigInteger.ZERO) > -1) {
                /* If the current address can be represented as [ startPCAddress + i * strideDistance ] where i is an integer >= 0 */
                currentState = StrideDetectionState.STRONG_STRIDE;
            } else {
                currentState = StrideDetectionState.WEAK_STRIDE;
            }
            prevPCAddress = newAddress;

        }

        return (currentState.getStateID() - prevState.getStateID());
    }

    /* Setting the FSM state to FirstObserved  */
    public void setToFirstObserved(BigInteger observedAddress) {
        currentState = StrideDetectionState.FIRST_OBSERVED;
        startAddress = observedAddress;
        prevPCAddress = BigInteger.ZERO;
        strideDistance = BigInteger.ZERO;
    }
}
