package de.backend.snakefever;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoServer;

public final class ServerWrapper {
    /** the jetty server */
    private final Server server;
    /** the jetty server connector */
    private final ServerConnector connector;

    /** the engine io settings */
    private final EngineIoServerOptions eioOptions;
    /** the engine io server */
    private final EngineIoServer eioServer;

    /** the socket io server */
    private final SocketIoServer socketIoServer;

    /**
     * This class contains code to start the webserver and socket io handler.
     * @param port
     * @param allowedCorsOrigins
     */
    public ServerWrapper(int port, String[] allowedCorsOrigins) {
        this.server = new Server();
        this.connector = new ServerConnector(server);
        this.connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase(SnakeFever.class.getClassLoader().getResource("WEB-INF").toExternalForm());

        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");

        // add the default servlet to the main path
        context.addServlet(DefaultServlet.class, "/");

        // engine io settings
        eioOptions = EngineIoServerOptions.newFromDefault();
        eioOptions.setAllowedCorsOrigins(allowedCorsOrigins);
        
        // initialize the engine io server
        eioServer = new EngineIoServer(eioOptions);
        // initialize the socket io server
        socketIoServer = new SocketIoServer(eioServer);
        
        // socket io servlet for http connections
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                eioServer.handleRequest(new HttpServletRequestWrapper(request) {
                    @Override
                    public boolean isAsyncSupported() {
                        return false;
                    }
                }, response);
            }
        }), "/socket.io/*");

        // socket io handler for websocket connections
        try {
            WebSocketUpgradeFilter webSocketUpgradeFilter = WebSocketUpgradeFilter.configureContext(context);
            webSocketUpgradeFilter.addMapping(
                    new ServletPathSpec("/socket.io/*"),
                    (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(eioServer));
        } catch (ServletException ex) {
            ex.printStackTrace();
        }

        // finally make the handlers
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] { context });
        server.setHandler(handlerList);
    }

    /**
     * Starts the server.
     * @throws Exception
     */
    public void startServer() throws Exception {
        server.start();
    }

    /**
     * Stops the server.
     * @throws Exception
     */
    public void stopServer() throws Exception {
        server.stop();
    }

    public SocketIoServer getSocketIoServer() {
        return socketIoServer;
    }
    
}
