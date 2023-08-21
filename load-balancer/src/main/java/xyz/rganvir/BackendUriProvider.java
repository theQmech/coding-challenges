package xyz.rganvir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BackendUriProvider {
    private static final String healthPath = "/health";
    private final List<String> serverUris;
    private final List<Boolean> serverHealthy;
    private final AtomicInteger count;

    BackendUriProvider() {
        serverUris = getServerListFromResource();
        System.out.printf("Using servers [%s]%n", serverUris);

        serverHealthy = new CopyOnWriteArrayList<>();
        serverHealthy.addAll(Collections.nCopies(serverUris.size(), false));
        startPeriodicHealthCheck();

        count = new AtomicInteger();
    }

    String getNextUri() {
        for (int i = 0; i < serverUris.size(); ++i) {
            int nextIndex = getNextIndex();
            if (!serverHealthy.get(nextIndex)) {
                continue;
            }
            return serverUris.get(nextIndex);
        }
        throw new RuntimeException("No servers are healthy!");
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
                    System.out.printf("Health response from [%s] is [%s]%n", serverUris.get(i), healthResponse);
                    status = healthResponse.equals("Ok");
                } catch (Exception e) {
                    status = false;
                    e.printStackTrace();
                }
                serverHealthy.set(i, status);
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(healthCheck, 0, 10, TimeUnit.SECONDS);
    }

    private static List<String> getServerListFromResource() {
        try (var in = BackendUriProvider.class.getResourceAsStream("/servers.list")) {
            return new BufferedReader(new InputStreamReader(in)).lines()
                    .filter(x -> !x.isBlank())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
