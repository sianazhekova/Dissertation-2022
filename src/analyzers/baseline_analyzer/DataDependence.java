package analyzers.baseline_analyzer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum DataDependence {
    DEPNONE (-1),
    WR (0),
    WW (1),
    RW (2)
    ;

    private int depID;

    DataDependence(int i) {
        this.depID = i;
    }

    DataDependence() {
        this.depID = -1;
    }

    public int getDepID() {
        return this.depID;
    }

    @Contract("_ -> new")
    public static @NotNull String getStringDepType(@NotNull DataDependence depType) {
        switch (depType) {
            case WR:
                return new String("Write-After-Read");

            case WW:
                return new String("Write-After-Write");

            case RW:
                return new String("Read-After-Write");

            case DEPNONE:
                return new String("No Dependence");

            default:
                return "DEPENDENCE ERROR";
        }
    }
}
