package analyzers.readers;

import analyzers.baseline_analyzer.MemoryAccess;
import org.jetbrains.annotations.NotNull;
import java.util.*;


public class MemoryTracer {

    public Runtime runtimeInstance;

    private long totalMemory;
    private long maxMemory;
    private long freeMemory;
    private long allocatedMemory;
    private long prevAllocatedMemory;
    private long prevFreeMemory;
    private long initialTime;
    private long elapsedTime;

    private Date date = new Date();

    public MemoryTracer() {
        runtimeInstance = Runtime.getRuntime();

        totalMemory = runtimeInstance.totalMemory();
        maxMemory = runtimeInstance.maxMemory();
        freeMemory = runtimeInstance.freeMemory();
        allocatedMemory = totalMemory - freeMemory;

        prevAllocatedMemory = 0;
        prevFreeMemory = freeMemory;

        initialTime = date.getTime();
        elapsedTime = 0;
    }

    public void updateMemoryParams() {
        this.totalMemory = this.runtimeInstance.totalMemory();
        this.maxMemory = this.runtimeInstance.maxMemory();

        this.prevFreeMemory = this.freeMemory;
        this.freeMemory = this.runtimeInstance.freeMemory();
        this.prevAllocatedMemory = allocatedMemory;

        this.allocatedMemory = totalMemory - freeMemory;
        this.elapsedTime = date.getTime() - initialTime;
        this.freeMemory = maxMemory - totalMemory + freeMemory;
    }

    public long getInitialTime() { return initialTime; }

    public long getElapsedTime() { return elapsedTime; }

    public long getTotalMemory() { return totalMemory; }

    public long getMaxMemory() { return maxMemory; }

    public long getFreeMemory() { return freeMemory; }

    public long getPrevAllocatedMemory() { return prevAllocatedMemory; }

    public long getPrevFreeMemory() {
        return prevFreeMemory;
    }

    public long getAllocatedMemory() {
        return allocatedMemory;
    }

    public long getDiffFreeMemory() {
        return (freeMemory - prevFreeMemory);
    }

    public long getDiffAllocatedMemory() {
        return (allocatedMemory - prevAllocatedMemory);
    }

    public String getRuntimeMemoryStatistics(String message) {

        return String.format("%s : Time Elapsed %s milliseconds \t Max Memory %s bytes \t Total Memory %s bytes \t Free Memory %s bytes \t ∆Free Memory %s bytes \t Allocated Memory %s bytes \t ∆Allocated Memory %s bytes",
                message,
                this.elapsedTime,
                this.maxMemory,
                this.totalMemory,
                this.freeMemory,
                this.getDiffFreeMemory(),
                this.allocatedMemory,
                this.getDiffAllocatedMemory()
        );
    }

    public void printMemoryStatistics(String message) {
        System.out.println(getRuntimeMemoryStatistics(message));
    }

}
