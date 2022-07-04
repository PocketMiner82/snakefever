package de.backend.snakefever.messageConstants;

public final class RoomMessageConstants {
    // EVENTS
    public static final String EVENT_CREATE_REQUEST = "room_create_request";
    public static final String EVENT_JOIN_REQUEST = "room_join_request";
    public static final String EVENT_JOIN_SUCCESS = "room_join_success";
    public static final String EVENT_JOIN = "room_join";
    public static final String EVENT_LEAVE = "room_leave";

    // ERRORS
    public static final String ERROR_ID_GENERATION_FAILED = "room_id_generation_failed";
    public static final String ERROR_INVALID_ID = "room_invalid_id";
}
