package xyz.rganvir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;


public enum Utils {
    ;

    public static HttpServer createServer(int port, HttpHandler handler) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", handler);
        return server;
    }

    public static String performGet(String uri) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static void respondText(HttpExchange exchange, String text) throws IOException {
        exchange.sendResponseHeaders(200, text.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(text.getBytes());
        os.close();
        exchange.close();
    }

    public static int getSystemProperty(String key, int defaultValue) {
        String value = System.getenv(key);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }
}