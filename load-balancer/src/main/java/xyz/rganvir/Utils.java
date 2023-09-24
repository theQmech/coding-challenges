package xyz.rganvir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;


public enum Utils {
    ;
    static OpenTelemetrySdk setupOtel() {
        PeriodicMetricReader loggingReader = PeriodicMetricReader.builder(LoggingMetricExporter.create())
                .setInterval(Duration.ofSeconds(5))
                .build();
        PeriodicMetricReader exporterReader = PeriodicMetricReader.builder(
                        OtlpHttpMetricExporter.builder().setEndpoint("http://localhost:9000").build())
                .setInterval(Duration.ofSeconds(5))
                .build();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                        .registerMetricReader(loggingReader)
                        .registerMetricReader(exporterReader)
                        .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setMeterProvider(sdkMeterProvider)
                .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(openTelemetrySdk::close));

        return openTelemetrySdk;
    }

    static void setupLogging() {
        try {
            LogManager.getLogManager().readConfiguration(
                    LoadBalancerMain.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            System.out.println("Unable to configure logging. Using default settings.");
        }
    }

    public static HttpServer createServer(int port, int nThreads, HttpHandler rootHandler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", rootHandler);
        ExecutorService executor = Utils.getSystemPropertyBool("VTHREADS", false) ?
                Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(nThreads);
        server.setExecutor(executor);
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

    public static int getSystemPropertyInt(String key, int defaultValue) {
        String value = System.getProperty(key);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }

    public static boolean getSystemPropertyBool(String key, boolean defaultValue) {
        String value = System.getProperty(key);
        return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static String getUID(int size) {
        return UUID.randomUUID().toString().substring(0, size);
    }
}