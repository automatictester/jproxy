package uk.co.automatictester.jproxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.automatictester.jproxy.rule.ProxyRule;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
class ProxyServer implements Runnable {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final ProxyConfig config;
    private final ProxyRuleList rules;
    private ExecutorService inboundPool;
    private ServerSocket serverSocket;
    private int proxyPort;

    public void run() {
        int configPort = config.getProxyPort();
        int connectionQueueSize = config.getConnectionQueueSize();
        int inboundPoolSize = config.getInboundPoolSize();

        inboundPool = Executors.newFixedThreadPool(inboundPoolSize);
        AtomicInteger connectionId = new AtomicInteger(0);

        try {
            serverSocket = new ServerSocket(configPort, connectionQueueSize);
            proxyPort = serverSocket.getLocalPort();
            log.info("Starting proxy localhost:{} -> {}:{}", proxyPort,
                    config.getTargetHost(), config.getTargetPort());
            latch.countDown();
            while (true) {
                Socket inboundSocket = serverSocket.accept();

                int cid = connectionId.incrementAndGet();
                ProxyRule rule = rules.get();
                Callable<Void> task = new ProxyTask(inboundSocket, cid, rule, config);

                inboundPool.submit(task);
            }
        } catch (BindException e) {
            log.error("Error: {}", e.getMessage());
        } catch (SocketException e) {
            // accept() will throw when closing ServerSocket from another thread
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            proxyPort = 0;
            closeServerSocket();
            inboundPool.shutdownNow();
            latch.countDown();
        }
    }

    public boolean stop() {
        int inboundThreads = ((ThreadPoolExecutor) inboundPool).getActiveCount();
        int timeout = config.getInboundPoolTerminationTimeout();
        log.info("Active inbound connections: {}. Attempting to shut down the inbound connection pool with a timeout of {}ms...",
                inboundThreads, timeout);
        try {
            proxyPort = 0;
            closeServerSocket();
            inboundPool.shutdownNow();
            boolean terminatedBeforeTimeout = inboundPool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
            if (terminatedBeforeTimeout) {
                log.info("Inbound connection pool terminated before timeout");
            } else {
                log.info("Inbound connection pool terminated after timeout");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void waitUntilStarted() throws InterruptedException {
        latch.await();
    }

    public boolean isStarted() {
        return inboundPool != null
                && serverSocket != null
                && proxyPort > 0;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public int getInboundPoolActiveThreadCount() {
        return ((ThreadPoolExecutor) inboundPool).getActiveCount();
    }

    private void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
