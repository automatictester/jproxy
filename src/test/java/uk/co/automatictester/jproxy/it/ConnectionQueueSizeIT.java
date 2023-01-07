package uk.co.automatictester.jproxy.it;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.co.automatictester.jproxy.JProxy;
import uk.co.automatictester.jproxy.ProxyConfig;
import uk.co.automatictester.jproxy.ProxyRuleList;
import uk.co.automatictester.jproxy.util.TrustAllSSLSocketFactory;
import uk.co.automatictester.jproxy.util.WebServer;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConnectionQueueSizeIT {

    private JProxy proxy;
    private WebServer webServer;

    @Test
    public void testConnectionQueueSizeOverflow() throws InterruptedException {
        int targetPort = webServer.getHttpsPort();
        int connectionQueueSize = 1;
        int proxyPort = startProxy(targetPort, connectionQueueSize);

        // multiple connections opened at the same time should overflow accept queue of size 1
        int connectionCount = connectionQueueSize * 8;
        ExecutorService executor = Executors.newFixedThreadPool(connectionCount);
        ((ThreadPoolExecutor) executor).prestartAllCoreThreads();

        List<Callable<Void>> connections = new ArrayList<>();
        for (int i = 0; i < connectionCount; i++) {
            connections.add(() -> {
                connectAndVerifyDN(proxyPort);
                return null;
            });
        }
        List<Future<Void>> results = executor.invokeAll(connections);

        // as a result, some of these connections will error out
        int errorCount = 0;
        for (int i = 0; i < connectionCount; i++) {
            try {
                results.get(i).get();
            } catch (Exception e) {
                errorCount++;
            }
        }

        assertThat(errorCount).isGreaterThan(0);

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }

    @Test
    public void testConnectionQueueSizeNoOverflow() throws InterruptedException {
        int targetPort = webServer.getHttpsPort();
        int connectionQueueSize = 4;
        int proxyPort = startProxy(targetPort, connectionQueueSize);

        // 4 connections opened at the same time will NOT overflow accept queue of size 4
        ExecutorService executor = Executors.newFixedThreadPool(connectionQueueSize);
        ((ThreadPoolExecutor) executor).prestartAllCoreThreads();

        List<Callable<Void>> connections = new ArrayList<>();
        for (int i = 0; i < connectionQueueSize; i++) {
            connections.add(() -> {
                connectAndVerifyDN(proxyPort);
                return null;
            });
        }
        List<Future<Void>> results = executor.invokeAll(connections);

        // as a result, none of these connections will error out
        int errorCount = 0;
        for (int i = 0; i < connectionQueueSize; i++) {
            try {
                results.get(i).get();
            } catch (Exception e) {
                errorCount++;
            }
        }

        assertThat(errorCount).isZero();

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }

    @BeforeMethod
    public void setup() {
        webServer = new WebServer();
    }

    @AfterMethod
    public void cleanup() throws InterruptedException {
        proxy.stop();
        webServer.stop();
    }

    private int startProxy(int targetPort, int connectionQueueSize) throws InterruptedException {
        ProxyConfig config = ProxyConfig.builder()
                .targetPort(targetPort)
                .connectionQueueSize(connectionQueueSize)
                .inboundPoolSize(1)
                .build();

        ProxyRuleList rules = new ProxyRuleList().addDelayConnect(2000);

        proxy = new JProxy(config, rules);
        return proxy.start();
    }

    private void connectAndVerifyDN(int proxyPort) {
        try {
            String host = "127.0.0.1";
            SSLSocketFactory sslSocketFactory = TrustAllSSLSocketFactory.createInstance();
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket();
            SocketAddress address = new InetSocketAddress(host, proxyPort);
            socket.connect(address);
            SSLSession sslSession = socket.getSession();
            String dn = sslSession.getPeerPrincipal().toString();
            socket.close();
            assertThat(dn).isEqualTo("CN=localhost");
            log.info("Client connection succeeded on port {}", socket.getLocalPort());
        } catch (IOException e) {
            log.error("Client connection failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
