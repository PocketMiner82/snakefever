package de.backend.snakefever;

import java.util.ArrayList;
import java.util.List;

import de.backend.snakefever.messageConstants.RoomMessageConstants;

public class Room {
    public static final int MAX_PLAYERS = 8;

    private final List<Player> players = new ArrayList<>();

    private String id;
    private boolean quickplay;


    public Room(String id, boolean quickplay) {
        this.id = id;
        this.quickplay = quickplay;
    }

    public boolean isFull() {
        return this.players.size() >= MAX_PLAYERS;
    }

    public boolean isQuickplay() {
        return this.quickplay;
    }

    public String getId() {
        return this.id;
    }

    public void tick() {
        for (Player player : this.players) {
            player.tick();
        }
    }

    public void removePlayer(Player player) {
        this.players.remove(player);

        SnakeFever.ns.broadcast(this.id, RoomMessageConstants.EVENT_LEAVE, player.getName());
        SnakeFever.LOGGER.info("Player " + player.getName() + " left room " + this.getId());
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        
        SnakeFever.ns.broadcast(this.id, RoomMessageConstants.EVENT_JOIN, player.getName());
        SnakeFever.LOGGER.info("Player " + player.getName() + " joined room " + this.getId());
    }
}
