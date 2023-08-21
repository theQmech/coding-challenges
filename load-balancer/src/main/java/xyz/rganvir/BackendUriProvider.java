package xyz.rganvir;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BackendUriProvider {
    List<String> serverUris;
    AtomicInteger count;

    BackendUriProvider() {
        serverUris = List.of("http://localhost:8801", "http://localhost:8802", "http://localhost:8803");
        count = new AtomicInteger();
    }
    String getNextUri() {
        int nextIndex = count.getAndUpdate(x -> (x + 1) % serverUris.size());
        return serverUris.get(nextIndex);
    }
}
