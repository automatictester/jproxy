package uk.co.automatictester.jproxy.it;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import uk.co.automatictester.jproxy.JProxy;
import uk.co.automatictester.jproxy.ProxyConfig;
import uk.co.automatictester.jproxy.ProxyRuleList;
import uk.co.automatictester.jproxy.util.TrustAllSSLSocketFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Slf4j
public class ResourceLeakIT {

    private JProxy proxy;

    @Test
    public void testThreadLeak() throws InterruptedException, IOException {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        int proxyPort = 8081;
        int inboundPoolSize = 20;
        ProxyConfig config = ProxyConfig.builder()
                .proxyPort(proxyPort)
                .targetPort(8080)
                .inboundPoolSize(inboundPoolSize)
                .build();
        ProxyRuleList rules = new ProxyRuleList().addConnect();
        proxy = new JProxy(config, rules);

        int currentThreadCountA = threadBean.getThreadCount();
        assertThat(currentThreadCountA).isLessThanOrEqualTo(20);
        log.info("Current thread count A: {}", currentThreadCountA);

        proxy.start();
        int currentThreadCountB = threadBean.getThreadCount();
        assertThat(currentThreadCountB).isEqualTo(currentThreadCountA + 1);
        log.info("Current thread count B: {}", currentThreadCountB);

        for (int i = 0; i < inboundPoolSize * 2; i++) {
            connectAndFail(proxyPort);
        }
        int currentThreadCountC = threadBean.getThreadCount();
        assertThat(currentThreadCountC).isEqualTo(currentThreadCountB + inboundPoolSize);
        log.info("Current thread count C: {}", currentThreadCountC);

        proxy.stop();
        int currentThreadCountD = threadBean.getThreadCount();
        assertThat(currentThreadCountD).isEqualTo(currentThreadCountA);
        log.info("Current thread count D: {}", currentThreadCountD);

        for (int i = 0; i < 20; i++) {
            proxy.start();
            proxy.stop();
        }
        int currentThreadCountE = threadBean.getThreadCount();
        assertThat(currentThreadCountE).isEqualTo(currentThreadCountA);
        log.info("Current thread count E: {}", currentThreadCountE);

        int peakThreadCount = threadBean.getPeakThreadCount();
        log.info("Peak thread count: {}", peakThreadCount);
        assertThat(peakThreadCount).isEqualTo(currentThreadCountC);
    }

    @AfterMethod
    public void cleanup() throws InterruptedException {
        proxy.stop();
    }

    private void connectAndFail(int proxyPort) throws IOException {
        String host = "localdev.me";
        SSLSocketFactory sslSocketFactory = TrustAllSSLSocketFactory.createInstance();
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket();
        SocketAddress address = new InetSocketAddress(host, proxyPort);
        socket.connect(address);
        SSLSession sslSession = socket.getSession();
        assertThatExceptionOfType(SSLPeerUnverifiedException.class).isThrownBy(sslSession::getPeerPrincipal);
        socket.close();
    }
}
