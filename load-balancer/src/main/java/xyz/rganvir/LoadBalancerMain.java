package xyz.rganvir;

import xyz.rganvir.loadbalancer.LoadBalancer;

import java.io.IOException;
import java.util.logging.Logger;

import static xyz.rganvir.Utils.setupLogging;

public class LoadBalancerMain {
    static {
        setupLogging();
    }

    private static final Logger LOGGER = Logger.getLogger(LoadBalancerMain.class.getName());

    public static void main(String[] args) throws IOException {
        int port = Utils.getSystemPropertyInt("SERVER_PORT", 8800);
        int nThreads = Utils.getSystemPropertyInt("NTHREADS", 200);
        LoadBalancer lb = new LoadBalancer(port, nThreads);
        lb.start();
        LOGGER.info("Load balancer running on [%d]".formatted(port));
    }
}
