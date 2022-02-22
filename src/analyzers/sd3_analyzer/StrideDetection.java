package analyzers.sd3_analyzer;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class StrideDetection {

    private StrideDetectionState currentState;
    private BigInteger strideDistance;
    private BigInteger startPCAddress;
    private BigInteger prevPCAddress;


    public StrideDetection() {
        strideDistance = BigInteger.ZERO;
        startPCAddress = BigInteger.ZERO;
        prevPCAddress = BigInteger.ZERO;

        currentState = StrideDetectionState.START;
    }

    public StrideDetectionState getCurrentState() { return currentState; }

    public BigInteger getStrideDistance() { return strideDistance; }

    public BigInteger getStartPCAddress() { return startPCAddress; }

    public BigInteger getPrevPCAddress() { return prevPCAddress; }

    public AccessCollectionType getPointOrStride() {
        AccessCollectionType accessType;
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

    public boolean isAPoint() { return getPointOrStride() == AccessCollectionType.POINT; }

    public boolean isAStride() { return getPointOrStride() == AccessCollectionType.STRIDE; }

    /* Try with both strictly increasing/decreasing sequences per stride as well as non-monotonic ones such as [10, 14, 18, 14, 18, 22, 18, 22, 26].
       Will evaluate performance of the former against the latter. */

    public int updateFSMStateMonotonic(@NotNull BigInteger newAddress) {
        BigInteger addressDiff = newAddress.subtract(prevPCAddress);
        StrideDetectionState prevState = currentState;

        if (currentState.equals(StrideDetectionState.START)) {
            /* The system is at the Starting State of the Finite State Machine */

            startPCAddress = newAddress;
            currentState = StrideDetectionState.FIRST_OBSERVED;
        } else if (currentState.equals(StrideDetectionState.FIRST_OBSERVED)) {
            /* The system is at the First Observed State of the Finite State Machine */

            strideDistance = newAddress.subtract(startPCAddress);
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
        BigInteger addressDiff = newAddress.subtract(startPCAddress);
        StrideDetectionState prevState = currentState;

        if (currentState.equals(StrideDetectionState.START)) {
            /* The system is at the Starting State of the Finite State Machine */

            startPCAddress = newAddress;
            currentState = StrideDetectionState.FIRST_OBSERVED;
        } else if (currentState.equals(StrideDetectionState.FIRST_OBSERVED)) {
            /* The system is at the First Observed State of the Finite State Machine */

            prevPCAddress = newAddress;
            strideDistance = newAddress.subtract(startPCAddress);
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

    public void setToFirstObserved(BigInteger observedAddress) {
        currentState = StrideDetectionState.FIRST_OBSERVED;
        startPCAddress = observedAddress;
        prevPCAddress = BigInteger.ZERO;
        strideDistance = BigInteger.ZERO;
    }
}
