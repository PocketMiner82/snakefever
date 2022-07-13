/*
REGEX TO SEARCH FOR JAVA STYLE COMMENTS: "    /// ((.|\n)+?)public"
REPLACE WITH: "    /// <summary>\n    /// $1/// </summary>\n    public"

REGEX TO SEARCH FOR @see: "\n    /// @see MessageConstants#(.*)"
REGEX TO REPLACE: "<br/>\n    /// See: <see cref="$1"/>"
*/


/// <summary>
/// This class contains possible messages that can be send via SocketIO.<br/>
/// If the field name ends with REQUEST, then the client emits messages to this event and the server listens to it.<br/>
/// If it ends with RESPONSE, then the client listens and the server emits.<br/>
/// If it ends with BROADCAST, it was sent from the server to all players in a room.
/// </summary>
public class MessageConstants
{
    //
    // GENERIC EVENTS
    //



    /// <summary>
    /// Event for sending errors to the client.<br/>
    /// Arguments (1):<br/>
    /// - string: error message (Anything starting with ERROR)
    /// </summary>
    public const String EVENT_ERROR_RESPONSE = "error";

    /// <summary>
    /// Only Serverside (no client emit): Called from SocketIO, when a client disconnects.<br/>
    /// Arguments: 0
    /// </summary>
    public const String SERVERSIDE_EVENT_DISCONNECT = "disconnect";

    /// <summary>
    /// Error message if the player sent invalid data.<br/>
    /// See: <see cref="EVENT_ERROR_RESPONSE"/>
    /// </summary>
    public const String ERROR_INVALID_DATA = "error_invalid_data";



    //
    // PLAYER EVENTS
    //



    /// <summary>
    /// Event for requesting a name change.<br/>
    /// Arguments (1):<br/>
    /// - string: new name, max 16 chars
    /// </summary>
    public const String EVENT_PLAYER_SET_NAME_REQUEST = "player_set_name_request";

    /// <summary>
    /// Response event for a name change request.<br/>
    /// Arguments (1):<br/>
    /// - string: new name or a (future) error starting with ERROR_PLAYER
    /// </summary>
    public const String EVENT_PLAYER_SET_NAME_RESPONSE = "player_set_name_response";

    /// <summary>
    /// Event for requesting to apply e.g. player input on the next tick.
    /// </summary>
    public const String EVENT_PLAYER_INPUT_REQUEST = "player_input_request";



    //
    // ROOM EVENTS
    //



    /// <summary>
    /// Event for requesting a room creation.<br/>
    /// Arguments: 0<br/>
    /// </summary>
    public const String EVENT_ROOM_CREATE_REQUEST = "room_create_request";

    /// <summary>
    /// Event for requesting to join an existing room.<br/>
    /// Arguments (1):<br/>
    /// - string: room id to join or <see cref="ROOM_QUICKPLAY"/> if the room should be a quickplay room.
    /// </summary>
    public const String EVENT_ROOM_JOIN_REQUEST = "room_join_request";

    /// <summary>
    /// Message for joining a quickplay room.<br/>
    /// See: <see cref="EVENT_ROOM_JOIN_REQUEST"/>
    /// </summary>
    public const String ROOM_QUICKPLAY = "room_quickplay";

    /// <summary>
    /// Response event for a room join request.<br/>
    /// Arguments (1):<br/>
    /// - string: the id of the joined room or an error starting with ERROR_ROOM
    /// </summary>
    public const String EVENT_ROOM_JOIN_RESPONSE = "room_join_response";

    /// <summary>
    /// Event broadcasting a player joining a room.<br/>
    /// Arguments (1):<br/>
    /// - string: name of player who joined the room
    /// </summary>
    public const String EVENT_ROOM_PLAYER_JOIN_BROADCAST = "room_player_join_broadcast";

    /// <summary>
    /// Event for requesting to leave a room.<br/>
    /// Arguments: 0
    /// </summary>
    public const String EVENT_ROOM_LEAVE_REQUEST = "room_leave_request";

    /// <summary>
    /// Response event for a room join request.<br/>
    /// Arguments (1):<br/>
    /// - bool: leave success?
    /// </summary>
    public const String EVENT_ROOM_LEAVE_RESPONSE = "room_leave_response";

    /// <summary>
    /// Event broadcasting a player left a room.<br/>
    /// Arguments (1):<br/>
    /// - string: name of player who left the room
    /// </summary>
    public const String EVENT_ROOM_PLAYER_LEAVE_BROADCAST = "room_player_leave_broadcast";

    /// <summary>
    /// Error message if the room id generation failed.<br/>
    /// See: <see cref="EVENT_ERROR_RESPONSE"/>
    /// </summary>
    public const String ERROR_ROOM_ID_GENERATION_FAILED = "error_room_id_generation_failed";

    /// <summary>
    /// Error message if the room id doesn't exist or is invalid.<br/>
    /// See: <see cref="EVENT_ERROR_RESPONSE"/>
    /// </summary>
    public const String ERROR_ROOM_INVALID_ID = "error_room_invalid_id";

    /// <summary>
    /// Error message if the room is full.<br/><br/>
    /// See: <see cref="EVENT_ERROR_RESPONSE"/>
    /// </summary>
    public const String ERROR_ROOM_FULL = "error_room_full";
}