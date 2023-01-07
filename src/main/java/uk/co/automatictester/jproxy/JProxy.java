package uk.co.automatictester.jproxy;

import lombok.extern.slf4j.Slf4j;
import uk.co.automatictester.jproxy.util.DaemonThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JProxy {

    private final ProxyConfig config;
    private final ProxyRuleList rules;
    private ProxyServer server;
    private ExecutorService acceptPool;
    private ExecutorService monitorPool;

    /**
     * Constructs a proxy, without starting it.
     *
     * @param config proxy configuration.
     * @param rules  connection handling rules.
     */
    public JProxy(ProxyConfig config, ProxyRuleList rules) {
        this.config = config;
        this.config.validate();
        this.rules = new ProxyRuleList(rules);
    }

    /**
     * Returns proxy port number.
     *
     * @return proxy port number.
     */
    public synchronized int getProxyPort() {
        verifyIfStarted();
        return server.getProxyPort();
    }

    /**
     * Starts the proxy.
     *
     * @return proxy port number.
     */
    public synchronized int start() throws InterruptedException {
        if (isStarted()) {
            log.warn("Already started");
            return server.getProxyPort();
        }

        server = new ProxyServer(config, rules);
        ThreadFactory daemonThreadFactory = DaemonThreadFactory.getInstance();
        acceptPool = Executors.newSingleThreadExecutor(daemonThreadFactory);
        acceptPool.execute(server);

        int monitorFrequency = config.getMonitorFrequency();
        if (monitorFrequency != 0) {
            ProxyMonitor monitor = new ProxyMonitor(this, monitorFrequency);
            monitorPool = Executors.newSingleThreadExecutor(daemonThreadFactory);
            monitorPool.execute(monitor);
        }

        server.waitUntilStarted();
        if (server.getProxyPort() == 0) throw new IllegalStateException("Unable to create server socket");
        return server.getProxyPort();
    }

    /**
     * Stops the proxy.
     *
     * @return true if proxy was started before invoking this method.
     */
    public synchronized boolean stop() throws InterruptedException {
        if (!isStarted()) {
            log.warn("Already stopped");
            return false;
        } else {
            server.stop();
            terminateThreadPool(acceptPool);
            terminateThreadPool(monitorPool);
            return true;
        }
    }

    private void terminateThreadPool(ExecutorService pool) throws InterruptedException {
        if (pool != null) {
            pool.shutdownNow();
            pool.awaitTermination(100, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Returns number of active threads in inbound pool.
     *
     * @return number of active threads in inbound pool.
     */
    public synchronized int getInboundPoolActiveThreadCount() {
        verifyIfStarted();
        return server.getInboundPoolActiveThreadCount();
    }

    private boolean isStarted() {
        return server != null
                && server.isStarted();
    }

    private void verifyIfStarted() {
        if (!isStarted()) throw new IllegalStateException("Not started");
    }
}
