package xyz.rganvir;

import com.sun.net.httpserver.HttpServer;

import java.util.logging.Logger;

import static xyz.rganvir.Utils.setupLogging;

public class BackendServer {
    static {
        setupLogging();
    }
    private static final Logger LOGGER = Logger.getLogger(BackendServer.class.getName());
    private static final String healthPath = "/health";

    public static void main(String[] args) throws Exception {
        String serverId = Utils.getUID(2);
        int port = Utils.getSystemPropertyInt("SERVER_PORT", 8801);

        HttpServer server = Utils.createServer(port, 5, exchange -> Utils.respondText(exchange, "Hello from Backend Server!"));
        server.createContext(healthPath, exchange -> Utils.respondText(exchange, "Ok"));
        server.start();

        LOGGER.info("[%s] Server started on [%d]".formatted(serverId, port));
    }
}
