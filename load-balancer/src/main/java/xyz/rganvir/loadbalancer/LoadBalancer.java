package xyz.rganvir.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import xyz.rganvir.BackendUriProvider;
import xyz.rganvir.Utils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LoadBalancer {
    private static final Logger LOGGER = Logger.getLogger(LoadBalancer.class.getName());
    private final HttpServer server;
    private final BackendUriProvider uriProvider;
    private final Map<String, AtomicInteger> pendingPerUri;
    private final ScheduledExecutorService statusPrintExecutor;

    public LoadBalancer(int port, int nThreads) throws IOException {
        this.server = Utils.createServer(port, nThreads, this::forwardResponse);
        this.uriProvider = new BackendUriProvider();
        this.pendingPerUri = new ConcurrentHashMap<>();
        this.statusPrintExecutor = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        server.start();
        this.statusPrintExecutor.scheduleAtFixedRate(this::printStats, 1, 5, TimeUnit.SECONDS);
    }

    private void printStats() {
        String result = "Statistics [" +
                pendingPerUri.entrySet().stream().map(entry -> String.format("<%s : %d>", entry.getKey(), entry.getValue().get())).collect(Collectors.joining(", ")) +
                " ]";
        LOGGER.info(result);
    }

    private void forwardResponse(HttpExchange exchange) {
        String requestId = Utils.getUID(6);
        LOGGER.info("[%s] Incoming request [%s %s %s]".formatted(requestId, exchange.getRemoteAddress(), exchange.getRequestMethod(), exchange.getProtocol()));

        String uri = uriProvider.getNextUri();
        recordUriRequest(requestId, uri);
        String response;
        try {
            response = Utils.performGet(uri);
        } catch (InterruptedException | IOException e) {
            response = "Unable to reach server";
        }

        try {
            Utils.respondText(exchange, response);
            LOGGER.info("[%s] Response sent [%s]".formatted(requestId, response));
        } catch (IOException e) {
            LOGGER.info("[%s] Unable to send response [%s]".formatted(requestId, e.getMessage()));
        }
        recordUriHandled(requestId, uri, response);
    }

    private void recordUriHandled(String requestId, String uri, String response) {
        pendingPerUri.get(uri).getAndUpdate(x -> x - 1);
        LOGGER.info("[%s] Handled request [%s]".formatted(requestId, response));
    }

    private void recordUriRequest(String requestId, String uri) {
        LOGGER.info("[%s] forwarding to [%s]".formatted(requestId, uri));
        pendingPerUri.computeIfAbsent(uri, unused -> new AtomicInteger()).incrementAndGet();
    }
}
