package de.backend.snakefever;

import de.backend.snakefever.messageConstants.MessageConstants;
import de.backend.snakefever.messageConstants.PlayerMessageConstants;
import de.backend.snakefever.messageConstants.RoomMessageConstants;
import io.socket.socketio.server.SocketIoSocket;

public class Player {
    private final SocketIoSocket socket;
    private final Server server;
    private Room room;

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
        socket.on(RoomMessageConstants.EVENT_CREATE_REQUEST, args -> onCreateRoomRequest(args));
        socket.on(RoomMessageConstants.EVENT_JOIN_REQUEST, args -> onRoomJoinRequest(args));
        socket.on(PlayerMessageConstants.EVENT_SET_NAME, args -> onPlayerNameSetRequest(args));
        socket.on(MessageConstants.EVENT_DISCONNECT, args -> onDisconnect(args));
    }

    private void onCreateRoomRequest(Object... args) {
        if (args[0] instanceof Boolean) {
            Room room = this.getServer().createRoom((boolean) args[0]);
            if (room == null) {
                this.socket.emit(MessageConstants.EVENT_ERROR, RoomMessageConstants.ERROR_ID_GENERATION_FAILED);
                SnakeFever.LOGGER.error("Room ID generation failed for Player: " + this.getName());
            } else {
                this.joinRoom(room);
            }
        } else {
            SnakeFever.LOGGER.error("Player " + this.getName() + " tried to create room with non boolean quickplay argument.");
            this.socket.emit(MessageConstants.EVENT_ERROR, MessageConstants.ERROR_INVALID_DATA);
        }
    }

    private void onRoomJoinRequest(Object... args) {
        if (args[0] instanceof String) {
            if (!this.joinRoom(((String)args[0]).toLowerCase())) {
                this.socket.emit(MessageConstants.EVENT_ERROR, RoomMessageConstants.ERROR_INVALID_ID);
                SnakeFever.LOGGER.warn("Player " + this.getName() + " tried to join non existing room: " + ((String)args[0]).toLowerCase());
            }
        } else {
            SnakeFever.LOGGER.error("Player " + this.getName() + " tried to join room with non string room id argument.");
            this.socket.emit(MessageConstants.EVENT_ERROR, MessageConstants.ERROR_INVALID_DATA);
        }
    }

    private void onPlayerNameSetRequest(Object... args) {
        if (args[0] instanceof String) {
            String name = ((String) args[0]).substring(0, 16);
            if (this.getServer().isNameTaken(name)) {
                this.socket.emit(MessageConstants.EVENT_ERROR, PlayerMessageConstants.ERROR_NAME_TAKEN);
                SnakeFever.LOGGER.warn("Player " + this.getName() + " tried to change name to already existing name: " + name);
            } else {
                SnakeFever.LOGGER.info("Player " + this.getName() + " changed name to: " + name);
                this.name = name;
            }
        } else {
            SnakeFever.LOGGER.error("Tried to set player name with non string name argument.");
            this.socket.emit(MessageConstants.EVENT_ERROR, MessageConstants.ERROR_INVALID_DATA);
        }
    }

    private void onDisconnect(Object... args) {
        this.server.removePlayer(this);
    }

    public String getName() {
        return this.name;
    }

    /**
     * Updates the player position, etc.
     */
    public void tick() {

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
     * @returns if the room exists.
     */
    public boolean joinRoom(String roomId) {
        Room room = this.getServer().getRoomMap().get(roomId);
        // check if the room exists
        if (room == null) {
            return false;
        }

        // then join it
        this.joinRoom(room);

        return true;
    }

    /**
     * Returns if the player currently is in a room or not.
     */
    public boolean isIdle() {
        return this.room == null;
    }

    /**
     * Adds the player to an existing room.
     * @param room the room as an object.
     */
    public void joinRoom(Room room) {
        this.room = room;
        this.room.addPlayer(this);

        this.socket.joinRoom(room.getId());
        this.socket.emit(RoomMessageConstants.EVENT_JOIN_SUCCESS, room.getId());
    }

    /**
     * Leave the current room.
     * @return true if the leave was success, false if the player is in no room
     */
    public boolean leaveRoom() {
        this.socket.leaveAllRooms();

        if (this.room != null) {
            this.room.removePlayer(this);
            if (this.getSocket() != null) {
                this.getSocket().emit(RoomMessageConstants.EVENT_LEAVE, this.room.getId());
            }

            this.room = null;
            return true;
        }
        
        return false;
    }
}
