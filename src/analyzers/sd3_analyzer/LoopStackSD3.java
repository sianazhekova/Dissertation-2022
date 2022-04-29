package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.LoopInstance;
import analyzers.baseline_analyzer.LoopStack;
import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PointPC;
import analyzers.readers.MemBufferBlock;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoopStackSD3 extends LoopStack {

    Map<BigInteger, StrideDetection> mapOfDetectors;

    public LoopStackSD3(boolean withExceptions) {
        super(withExceptions);
        mapOfDetectors = new HashMap<>();
    }

    public LoopStackSD3() {
        super();
        mapOfDetectors = new HashMap<>();
    }

    public void encounterNewAccess(@NotNull List<MemBufferBlock> bufferBlockPair, long tripCount) {





    }

    public void addNewAccess(BigInteger refStartAddress, BigInteger sizeOfAccess, MemoryAccess readOrWrite, BigInteger PCAddress, long numTrips) {
        PointPC newPoint = new PointPC(refStartAddress, sizeOfAccess, readOrWrite, PCAddress);
        LoopInstanceSD3 topLoopInstance = (LoopInstanceSD3) stack.peek();
        // Record memory space here
        topLoopInstance.addNewMemoryAccess(newPoint, numTrips, mapOfDetectors.get(PCAddress));
        // Record memory space here
    }

    public void clearMapOfDetectors() { mapOfDetectors.clear();}

    public boolean deleteDetectorForKey(BigInteger PCKey) {
        if (mapOfDetectors.containsKey(PCKey)) {
            mapOfDetectors.remove(PCKey);
            return true;
        } else
           return false;
    }

}
