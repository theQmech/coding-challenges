package xyz.rganvir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class LoadBalancer {
    public static void main(String[] args) throws Exception {
        int port = Utils.getSystemProperty("SERVER_PORT", 8800);
        HttpServer server = Utils.createServer(port, LoadBalancer::forwardResponse);
        server.start();
    }

    private static void forwardResponse(HttpExchange exchange) throws IOException {
        String response;
        try {
            response = Utils.performGet("http://backend1:8801");
        } catch (Exception e) {
            response = "Unable to reach servers!";
            e.printStackTrace();;
        }

        System.out.println("Responding with: " + response);

        Utils.respondText(exchange, response);
    }
}
