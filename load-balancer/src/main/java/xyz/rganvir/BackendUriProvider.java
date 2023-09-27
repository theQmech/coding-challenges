package xyz.rganvir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class BackendUriProvider {
    private static final Logger LOGGER = Logger.getLogger(BackendUriProvider.class.getName());
    private static final String healthPath = "/health";
    private final List<String> serverUris;
    private final List<Boolean> serverHealthy;
    private final AtomicInteger count;

    public BackendUriProvider() {
        serverUris = getServerListFromResource();
        LOGGER.info("Using servers [%s]".formatted(serverUris));

        serverHealthy = new CopyOnWriteArrayList<>();
        serverHealthy.addAll(Collections.nCopies(serverUris.size(), false));
        startPeriodicHealthCheck();

        count = new AtomicInteger();
    }

    public String getNextUri() {
        for (int i = 0; i < serverUris.size(); ++i) {
            int nextIndex = getNextIndex();
            if (!serverHealthy.get(nextIndex)) {
                continue;
            }
            return serverUris.get(nextIndex);
        }
        LOGGER.info("No servers are healthy");
        return null;
    }

    private int getNextIndex() {
        return count.getAndUpdate(x -> (x + 1) % serverUris.size());
    }

    private void startPeriodicHealthCheck() {
        Runnable healthCheck = () -> {
            for (int i = 0; i < serverUris.size(); ++i) {
                boolean status;
                try {
                    String healthResponse = Utils.performGet(serverUris.get(i) + healthPath);
                    LOGGER.fine("Health response from [%s] is [%s]".formatted(serverUris.get(i), healthResponse));
                    status = healthResponse.equals("Ok");
                } catch (Exception e) {
                    status = false;
                    LOGGER.warning("Health check on [%s] failed. [%s]".formatted(serverUris.get(i), e.getMessage()));
                }
                serverHealthy.set(i, status);
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(healthCheck, 0, 10, TimeUnit.SECONDS);
    }

    private static List<String> getServerListFromResource() {
        try (var in = Objects.requireNonNull(BackendUriProvider.class.getResourceAsStream("/servers.list"))) {
            return new BufferedReader(new InputStreamReader(in)).lines()
                    .filter(x -> !x.isBlank())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
