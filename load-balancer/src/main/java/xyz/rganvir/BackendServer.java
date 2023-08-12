package xyz.rganvir;

import com.sun.net.httpserver.HttpServer;

public class BackendServer {
    public static void main(String[] args) throws Exception {
        int port = Utils.getSystemProperty("SERVER_PORT", 8800);
        HttpServer server = Utils.createServer(port, exchange -> Utils.respondText(exchange, "Hello World!"));
        server.start();
        System.out.println("Server started");
    }
}
