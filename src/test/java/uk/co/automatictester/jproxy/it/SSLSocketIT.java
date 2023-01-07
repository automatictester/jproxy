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

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SSLSocketIT {

    private JProxy proxy;
    private WebServer webServer;

    @Test
    public void testConnect() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addConnect();
        int proxyPort = startProxyAndGetPort(rules);

        connectAndVerifyDN(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = SSLPeerUnverifiedException.class)
    public void testDisconnect() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDisconnect();
        int proxyPort = startProxyAndGetPort(rules);

        connectAndVerifyDN(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = SSLPeerUnverifiedException.class)
    public void testDelayConnectTimeout() throws IOException, InterruptedException {
        int connectTimeout = 500;
        int readTimeout = 800;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayConnect(1100);
        int proxyPort = startProxyAndGetPort(rules);

        connectAndVerifyDN(connectTimeout, readTimeout, proxyPort);
    }

    @Test
    public void testDelayConnectNoTimeout() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayConnect(1100);
        int proxyPort = startProxyAndGetPort(rules);

        connectAndVerifyDN(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = SSLPeerUnverifiedException.class)
    public void testDelayDisconnectTimeout() throws IOException, InterruptedException {
        int connectTimeout = 500;
        int readTimeout = 800;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayDisconnect(1100);
        int proxyPort = startProxyAndGetPort(rules);

        connectAndVerifyDN(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = SSLPeerUnverifiedException.class)
    public void testDelayDisconnectNoTimeout() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayDisconnect(1100);
        int proxyPort = startProxyAndGetPort(rules);

        connectAndVerifyDN(connectTimeout, readTimeout, proxyPort);
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

    private void connectAndVerifyDN(int connectTimeout, int readTimeout, int proxyPort) throws IOException {
        String host = "127.0.0.1";
        SSLSocketFactory sslSocketFactory = TrustAllSSLSocketFactory.createInstance();
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket();
        socket.setSoTimeout(readTimeout);
        SocketAddress address = new InetSocketAddress(host, proxyPort);
        socket.connect(address, connectTimeout);
        SSLSession sslSession = socket.getSession();
        String dn = sslSession.getPeerPrincipal().toString();
        socket.close();
        assertThat(dn).isEqualTo("CN=localhost");
    }

    private int startProxyAndGetPort(ProxyRuleList rules) throws InterruptedException {
        int targetPort = webServer.getHttpsPort();

        ProxyConfig config = ProxyConfig.builder()
                .targetPort(targetPort)
                .build();

        proxy = new JProxy(config, rules);
        return proxy.start();
    }
}
