package analyzers.sd3_analyzer;

import org.jetbrains.annotations.NotNull;

/* An enumeration class for the types of a recognised memory access - a stride or a point memory access type,
    or none if it does not fall into neither category. */

public enum AccessCollectionType {
    NONE(-1),
    POINT(0),
    STRIDE(1)
    ;

    private int intID;

    AccessCollectionType(int i) {
        this.intID = i;
    }

    AccessCollectionType() {
        this.intID = -1;
    }

    public int getIntID() { return this.intID; }

    public static String getStringAccessCollectionType(@NotNull AccessCollectionType collectionType) { return collectionType.toString(); }
}
