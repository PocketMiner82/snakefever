package de.backend.snakefever;

import de.backend.snakefever.game.IngamePlayer;
import de.backend.snakefever.messageConstants.MessageConstants;
import io.socket.socketio.server.SocketIoSocket;

public class Player {
    private final SocketIoSocket socket;
    private final Server server;
    private Room room;

    private final IngamePlayer ingamePlayer = new IngamePlayer();

    private String name;

    /**
     * Player class, handling all player related things.
     * @param socket the socket io socket assigned to this player.
     */
    public Player(SocketIoSocket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.name = socket.getId();

        this.registerListeners();
    }

    private void registerListeners() {
        socket.on(MessageConstants.EVENT_ROOM_CREATE_REQUEST, args -> onCreateRoomRequest(args));
        socket.on(MessageConstants.EVENT_ROOM_JOIN_REQUEST, args -> onRoomJoinRequest(args));
        socket.on(MessageConstants.EVENT_PLAYER_SET_NAME_REQUEST, args -> onPlayerNameSetRequest(args));
        socket.on(MessageConstants.EVENT_ROOM_LEAVE_REQUEST, args -> onRoomLeaveRequest(args));
        socket.on(MessageConstants.SERVERSIDE_EVENT_DISCONNECT, args -> onDisconnect(args));
        socket.on(MessageConstants.EVENT_PLAYER_INPUT_REQUEST, args -> ingamePlayer.onPlayerInput(args));
    }

    private void onCreateRoomRequest(Object... args) {
        if (args[0] instanceof Boolean) {
            Room room = this.getServer().createRoom((boolean) args[0]);
            // tell the player, if the room id generation was not successfull
            if (room == null) {
                this.socket.emit(MessageConstants.EVENT_ROOM_JOIN_RESPONSE, MessageConstants.ERROR_ROOM_ID_GENERATION_FAILED);
                SnakeFever.LOGGER.error("Room ID generation failed for Player: " + this.getName());
            } else {
                this.joinRoom(room);
            }
        } else {
            SnakeFever.LOGGER.error("Player " + this.getName() + " tried to create room with non boolean quickplay argument.");
            this.socket.emit(MessageConstants.EVENT_ERROR_RESPONSE, MessageConstants.ERROR_INVALID_DATA);
        }
    }

    private void onRoomJoinRequest(Object... args) {
        if (args[0] instanceof String) {
            // tell the player if the room id is valid
            this.socket.emit(MessageConstants.EVENT_ROOM_JOIN_RESPONSE, this.joinRoom(((String)args[0]).toLowerCase()));
        } else {
            SnakeFever.LOGGER.error("Player " + this.getName() + " tried to join room with non string room id argument.");
            this.socket.emit(MessageConstants.EVENT_ERROR_RESPONSE, MessageConstants.ERROR_INVALID_DATA);
        }
    }

    private void onPlayerNameSetRequest(Object... args) {
        if (args[0] instanceof String) {
            String name = (String) args[0];
            // max 16 char name
            name = name.length() > 16 ? name.substring(0, 16) : name;

            this.socket.emit(MessageConstants.EVENT_PLAYER_SET_NAME_RESPONSE, name);
            SnakeFever.LOGGER.info("Player " + this.getName() + " changed name to: " + name);
            this.name = name;
        } else {
            SnakeFever.LOGGER.error("Tried to set player name with non string name argument.");
            this.socket.emit(MessageConstants.EVENT_ERROR_RESPONSE, MessageConstants.ERROR_INVALID_DATA);
        }
    }

    private void onRoomLeaveRequest(Object... args) {
        socket.emit(MessageConstants.EVENT_ROOM_LEAVE_RESPONSE, this.leaveRoom());
    }

    private void onDisconnect(Object... args) {
        this.server.removePlayer(this);
    }

    public String getName() {
        return this.name + "#" + this.socket.getId();
    }

    /**
     * Updates the player position, etc.
     */
    public void tick() {
        if (this.ingamePlayer != null && this.isIngame()) {
            this.ingamePlayer.tick();
        }
    }

    public Server getServer() {
        return this.server;
    }

    public SocketIoSocket getSocket() {
        return this.socket;
    }

    /**
     * Returns the player's room.
     * @return null, if there is no room assigned.
     */
    public Room getRoom() {
        return this.room;
    }

    /**
     * Adds the player to an existing room.
     * @param roomId the room id as a string.
     * @returns String if the join was success (room id) or not (room full, invalid id)
     */
    public String joinRoom(String roomId) {
        Room room = this.getServer().getRoomMap().get(roomId);
        // check if the room exists
        if (room == null) {
            return MessageConstants.ERROR_ROOM_INVALID_ID;
        } else if (room.isFull()) {
            return MessageConstants.ERROR_ROOM_FULL;
        }

        // then join it
        this.joinRoom(room);

        return room.getId();
    }

    /**
     * Returns if the player currently is in a room or not.
     */
    public boolean isIngame() {
        return this.room != null;
    }

    /**
     * Adds the player to an existing room.
     * @param room the room as an object.
     */
    public void joinRoom(Room room) {
        this.room = room;
        this.room.addPlayer(this);

        this.socket.joinRoom(room.getId());
        this.socket.emit(MessageConstants.EVENT_ROOM_JOIN_RESPONSE, this.room.getId());
    }

    /**
     * Leave the current room.
     * @return true if the leave was success, false if the player is in no room
     */
    public boolean leaveRoom() {
        this.socket.leaveAllRooms();

        if (this.room != null) {
            this.room.removePlayer(this);
            this.room = null;
            return true;
        }
        
        return false;
    }
}
