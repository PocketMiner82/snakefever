package de.backend.snakefever;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.socket.emitter.Emitter;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;

public class SnakeFever {
    // Define a static logger variable so that it references the
    // Logger instance named "Pong".
    public static final Logger LOGGER = LogManager.getLogger(SnakeFever.class);

    public static void main(String[] args) {
        final ServerWrapper serverWrapper = new ServerWrapper(3000, null); // null means "allow all" as stated in https://github.com/socketio/engine.io-server-java/blob/f8cd8fc96f5ee1a027d9b8d9748523e2f9a14d2a/engine.io-server/src/main/java/io/socket/engineio/server/EngineIoServerOptions.java#L26
        try {
            serverWrapper.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SocketIoServer server = serverWrapper.getSocketIoServer();
        SocketIoNamespace ns = server.namespace("/");
        
        ns.on("connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                SocketIoSocket socket = (SocketIoSocket) args[0];
                LOGGER.info("Client " + socket.getId() + " has connected.");

                socket.on("message", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        LOGGER.info("Client " + socket.getId() + " Receive:");
                        for (Object obj : args) {
                            LOGGER.info(obj);
                        }
                        socket.send("message", "test message", 1);
                    }
                });
                
            }
        });
    }
}