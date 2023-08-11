package xyz.rganvir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class LoadBalancer {
    public static void main(String[] args) throws Exception {
        HttpServer server = Utils.createServer(8800, LoadBalancer::forwardResponse);
        server.start();
    }

    private static void forwardResponse(HttpExchange exchange) throws IOException {
        String response;
        try {
            response = Utils.performGet("http://localhost:8801");
        } catch (Exception  e) {
            response = "Unable to reach servers!";
        }

        Utils.respondText(exchange, response);
    }
}
