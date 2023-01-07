package uk.co.automatictester.jproxy.it;

import org.testng.annotations.AfterMethod;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class ProxyLifecycleIT {

    private WebServer webServer;
    private JProxy proxy;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNoTargetPort() {
        ProxyConfig config = ProxyConfig.builder().build();
        ProxyRuleList rules = new ProxyRuleList().addConnect();
        new JProxy(config, rules);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetProxyPortBeforeStart() {
        JProxy proxy = getProxy();
        proxy.getProxyPort();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetProxyPortAfterStop() throws InterruptedException {
        JProxy proxy = getProxy();
        proxy.start();
        proxy.stop();
        proxy.getProxyPort();
    }

    @Test
    public void testStartStopTwice() throws InterruptedException {
        JProxy proxy = getProxy();

        int proxyPort = proxy.start();
        assertThat(proxyPort).isGreaterThan(0);

        boolean stopResult = proxy.stop();
        assertThat(stopResult).isTrue();

        proxyPort = proxy.start();
        assertThat(proxyPort).isGreaterThan(0);

        stopResult = proxy.stop();
        assertThat(stopResult).isTrue();
    }

    @Test
    public void testConnectAfterRestart() throws IOException, InterruptedException {
        webServer = new WebServer();
        int targetPort = webServer.getHttpsPort();

        ProxyConfig config = ProxyConfig.builder()
                .proxyPort(8081)
                .targetPort(targetPort)
                .build();

        ProxyRuleList rules = new ProxyRuleList().addConnect();

        proxy = new JProxy(config, rules);

        int proxyPort = proxy.start();
        connectAndVerifyDN(proxyPort);

        proxy.stop();
        proxy.start();
        connectAndVerifyDN(proxyPort);

        proxy.stop();
        proxy.start();
        connectAndVerifyDN(proxyPort);
    }

    @Test
    public void testStopBeforeStart() throws InterruptedException {
        JProxy proxy = getProxy();

        boolean stopResult = proxy.stop();
        assertThat(stopResult).isFalse();

        int proxyPort = proxy.start();
        assertThat(proxyPort).isGreaterThan(0);

        int getProxyPort = proxy.getProxyPort();
        assertThat(getProxyPort).isEqualTo(proxyPort);

        stopResult = proxy.stop();
        assertThat(stopResult).isTrue();
    }

    @Test
    public void testStartTwice() throws InterruptedException {
        JProxy proxy = getProxy();

        int proxyPortA = proxy.start();
        int proxyPortB = proxy.start();
        assertThat(proxyPortA == proxyPortB).isTrue();

        boolean stopResult = proxy.stop();
        assertThat(stopResult).isTrue();
    }

    @Test
    public void testStopTwice() throws InterruptedException {
        JProxy proxy = getProxy();

        boolean result = proxy.stop();
        assertThat(result).isFalse();

        result = proxy.stop();
        assertThat(result).isFalse();
    }

    @Test
    public void testStartOnUsedPort() throws InterruptedException {
        JProxy proxyA = getProxy();
        proxyA.start();

        JProxy proxyB = getProxy();
        assertThatIllegalStateException().isThrownBy(proxyB::start);

        proxyA.stop();
    }

    @AfterMethod
    public void cleanup() throws InterruptedException {
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
        if (proxy != null) {
            proxy.stop();
            proxy = null;
        }
    }

    private void connectAndVerifyDN(int proxyPort) throws IOException {
        String host = "localdev.me";
        SSLSocketFactory sslSocketFactory = TrustAllSSLSocketFactory.createInstance();
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket();
        SocketAddress address = new InetSocketAddress(host, proxyPort);
        socket.connect(address);
        SSLSession sslSession = socket.getSession();
        String dn = sslSession.getPeerPrincipal().toString();
        socket.close();
        assertThat(dn).isEqualTo("CN=localhost");
    }

    private JProxy getProxy() {
        ProxyConfig config = ProxyConfig.builder().proxyPort(8081).targetPort(8080).build();
        ProxyRuleList rules = new ProxyRuleList().addConnect();
        return new JProxy(config, rules);
    }
}
