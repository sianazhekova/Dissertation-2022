package analyzers.baseline_analyzer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;

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

    public static DataDependence getDependence( MemoryAccess firstAccess, MemoryAccess secondAccess) {
        if (firstAccess == MemoryAccess.READ && secondAccess == MemoryAccess.WRITE) {
            return WR;
        } else if (firstAccess == MemoryAccess.WRITE && secondAccess == MemoryAccess.WRITE) {
            return WW;
        } else if (firstAccess == MemoryAccess.WRITE && secondAccess == MemoryAccess.READ) {
            return RW;
        } else {
            return DEPNONE;
        }
    }

    @Contract(value = " -> new", pure = true)
    public static DataDependence @NotNull [] getDependenceTypes() {
        return new DataDependence[]{ DataDependence.RW, DataDependence.WW, DataDependence.WR };
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
