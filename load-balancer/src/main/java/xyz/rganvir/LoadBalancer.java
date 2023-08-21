package xyz.rganvir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class LoadBalancer {
    private static final BackendUriProvider uriProvider = new BackendUriProvider();

    public static void main(String[] args) throws Exception {
        int port = Utils.getSystemPropertyAsInt("SERVER_PORT", 8800);
        HttpServer server = Utils.createServer(port, LoadBalancer::forwardResponse);
        server.start();
        System.out.printf("Load balancer running on [%d]%n", port);
    }

    private static void forwardResponse(HttpExchange exchange) throws IOException {
        String requestId = Utils.getUID(4);
        System.out.printf("[%s] Incoming request from %s%n", requestId, exchange.getRemoteAddress());

        String response;
        try {
            String uri = uriProvider.getNextUri();
            System.out.printf("[%s] forwarding to [%s]\n", requestId, uri);
            response = Utils.performGet(uri);
        } catch (Exception e) {
            response = "Unable to reach servers!";
            e.printStackTrace();;
        }

        Utils.respondText(exchange, response);
        System.out.printf("[%s] Response sent [%s]\n", requestId, response);
    }
}
