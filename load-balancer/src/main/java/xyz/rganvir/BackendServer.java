package xyz.rganvir;

import com.sun.net.httpserver.HttpServer;

public class BackendServer {
    public static void main(String[] args) throws Exception {
        String serverId = Utils.getUID(2);
        int port = Utils.getSystemPropertyAsInt("SERVER_PORT", 8801);
        HttpServer server = Utils.createServer(port, exchange -> Utils.respondText(exchange, "Hello from Backend Server!"));
        server.start();
        System.out.printf("[%s] Server started on [%d] %n", serverId, port);
    }
}
