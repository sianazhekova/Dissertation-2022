package analyzers.readers;

import org.jetbrains.annotations.NotNull;

public enum EventType {
    INVALID (-1),
    START (0),
    END (1),
    LOAD (2),
    STORE (3)
    ;

    private int eventID;

    EventType(int i) {
        this.eventID = i;
    }

    EventType() {
        this.eventID = -1;
    }

    public int getEventID() {
        return this.eventID;
    }

    public static String getStringEventType(@NotNull EventType eventType) {
        return eventType.toString();
    }
}
