package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

public enum MemoryAccess {
    INVALID (-1),
    READ (0),
    WRITE (1)
    ;

    private int intID;

    MemoryAccess(int i) {
        this.intID = i;
    }

    MemoryAccess() {
        this.intID = -1;
    }

    public int getIntID() {
        return this.intID;
    }

    public static String getStringMemAccess(@NotNull MemoryAccess dataAccess) {
        return dataAccess.toString();
    }
}




