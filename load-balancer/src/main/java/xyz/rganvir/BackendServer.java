package xyz.rganvir;

import com.sun.net.httpserver.HttpServer;

public class BackendServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = Utils.createServer(8801, exchange -> Utils.respondText(exchange, "Hello World!"));
        server.start();
    }
}
