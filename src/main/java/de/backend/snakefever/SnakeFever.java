package de.backend.snakefever;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.backend.snakefever.socketio.ServerWrapper;
import io.socket.engineio.server.Emitter;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoSocket;

public class SnakeFever {
    // Define a static logger variable so that it references the
    // Logger instance named "SnakeFever".
    public static final Logger LOGGER = LogManager.getLogger(SnakeFever.class);

    public static final Server SERVER = new Server();

    private static final ScheduledExecutorService ticker = Executors.newScheduledThreadPool(10);

    public static SocketIoNamespace ns;

    public static void main(String[] args) {
        LOGGER.info("Starting SnakeFever...");

        // null means "allow all" as stated in https://bit.ly/3Gz8WX4
        final ServerWrapper serverWrapper = new ServerWrapper(3000, null);
        try {
            serverWrapper.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ns = serverWrapper.getSocketIoServer().namespace("/");

        ns.on("connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                SocketIoSocket socket = (SocketIoSocket) args[0];
                SERVER.registerPlayer(socket);

                /*socket.on("message", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        LOGGER.info("Client " + socket.getId() + " Receive:");
                        for (Object obj : args) {
                            LOGGER.info(obj);
                        }
                        socket.send("message", "test message", 1);
                    }
                });*/
            }
        });

        // tick the server
        ticker.scheduleAtFixedRate(() -> {
            SERVER.tick();
        }, 0, 50, TimeUnit.MILLISECONDS);

        LOGGER.info("Startup done!");
    }
}