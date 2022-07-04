package de.backend.snakefever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import de.backend.snakefever.messageConstants.RoomMessageConstants;
import io.socket.socketio.server.SocketIoSocket;

public class Server {
    private final Map<String, Room> rooms = new HashMap<>();
    private final List<Player> players = new ArrayList<>();

    /**
     * Adds a player to the server.
     * @param socket the player's socket.
     */
    public void registerPlayer(SocketIoSocket socket) {
        Player player = new Player(socket, this);
        this.players.add(player);
        SnakeFever.LOGGER.info("Player " + player.getName() + " connected.");
    }

    public Room createRoom(boolean quickplay) {
        String id = quickplay ? this.findQuickPlayRoomId() : this.findEmptyRoomId();

        Room room = null;

        if (!id.equals(RoomMessageConstants.ERROR_INVALID_ID)) {
            room = new Room(id, quickplay);
            this.rooms.put(id, room);
        }

        return room;
    }

    /**
     * Returns all the existing rooms.
     */
    public List<Room> getRooms() {
        return new ArrayList<>(this.rooms.values());
    }

    /**
     * Returns all the existing room ids.
     */
    public List<String> getRoomIds() {
        return new ArrayList<>(this.rooms.keySet());
    }

    /**
     * Returns all the room map with id and room object.
     */
    public Map<String, Room> getRoomMap() {
        return this.rooms;
    }

    /**
     * Searches for a quickplay room id.
     * @return a room, where quickplay is true and that is not full.
     */
    private String findQuickPlayRoomId() {
        for (Room room : this.getRooms()) {
            if (room.isQuickplay() && !room.isFull()) {
                return room.getId();
            }
        }

        return this.findEmptyRoomId();
    }

    private String findEmptyRoomId() {
        String randomId = "";

        // we try it for some time to generate the id
        for (int i = 0; i < 100; i++) {
            // generate a long, the hex string will be 8 chars long at maximum
            randomId = Long.toHexString(ThreadLocalRandom.current().nextLong(0xFFFFFFFFl + 0x1l));

            // look if there already is a room with that id
            if (!this.getRoomIds().contains(randomId))
                break;

            randomId = RoomMessageConstants.ERROR_ID_GENERATION_FAILED;
        }

        // ensure, that the id is 8 chars long
        int missingChars = 8 - randomId.length();
        for (int i = 0; i < missingChars; i++) {
            randomId = "0" + randomId;
        }

        return randomId;
    }

    /**
     * Get all the players on the whole server.
     * @return a list of all online players.
     */
    public List<Player> getPlayers() {
        return this.players;
    }

    /**
     * Removes a player from the server.
     */
    public void removePlayer(Player player) {
        player.leaveRoom();
        this.players.remove(player);
        SnakeFever.LOGGER.info("Player " + player.getName() + " (Socket ID: " + player.getSocket().getId() + ") disconnected.");
    }

    /**
     * Ticks all rooms and their players to update their position, etc.
     */
    public void tick() {
        this.rooms.values().forEach(room -> room.tick());
    }

    /**
     * Checks if a player with the given name already exists.
     * @param name the player name to check
     */
    public boolean isNameTaken(String name) {
        for (Player player : players) {
            if (player.getName().equals(name))
                return true;
        }

        return false;
    }
}
