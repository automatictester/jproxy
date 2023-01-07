package uk.co.automatictester.jproxy.it;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.co.automatictester.jproxy.JProxy;
import uk.co.automatictester.jproxy.ProxyConfig;
import uk.co.automatictester.jproxy.ProxyRuleList;
import uk.co.automatictester.jproxy.util.HttpsURLConnectionFactory;
import uk.co.automatictester.jproxy.util.WebServer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

@Slf4j
public class HttpsURLConnectionIT {

    private JProxy proxy;
    private WebServer webServer;

    @Test
    public void testConnect() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addConnect();
        int proxyPort = startProxy(rules);

        connect(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = {SocketException.class, SSLHandshakeException.class, SSLException.class})
    public void testDisconnect() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDisconnect();
        int proxyPort = startProxy(rules);

        connect(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = {SocketTimeoutException.class, SSLException.class})
    public void testDelayConnectTimeout() throws IOException, InterruptedException {
        int connectTimeout = 500;
        int readTimeout = 800;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayConnect(1100);
        int proxyPort = startProxy(rules);

        connect(connectTimeout, readTimeout, proxyPort);
    }

    @Test
    public void testDelayConnectNoTimeout() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayConnect(1100);
        int proxyPort = startProxy(rules);

        connect(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = {SocketTimeoutException.class, SSLException.class})
    public void testDelayDisconnectTimeout() throws IOException, InterruptedException {
        int connectTimeout = 500;
        int readTimeout = 800;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayDisconnect(1100);
        int proxyPort = startProxy(rules);

        connect(connectTimeout, readTimeout, proxyPort);
    }

    @Test(expectedExceptions = {SocketException.class, SSLHandshakeException.class, SSLException.class})
    public void testDelayDisconnectNoTimeout() throws IOException, InterruptedException {
        int connectTimeout = 2000;
        int readTimeout = 2000;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayDisconnect(1100);
        int proxyPort = startProxy(rules);

        connect(connectTimeout, readTimeout, proxyPort);
    }

    @BeforeMethod
    public void startup() {
        webServer = new WebServer();
    }

    @AfterMethod
    public void cleanup() throws InterruptedException {
        proxy.stop();
        webServer.stop();
    }

    private void connect(int connectTimeout, int readTimeout, int proxyPort) throws IOException {
        String host = "localhost";
        String url = String.format("https://%s:%s", host, proxyPort);
        HttpsURLConnection httpsUrlConnection = HttpsURLConnectionFactory.createInstance(url);
        try {
            httpsUrlConnection.setConnectTimeout(connectTimeout);
            httpsUrlConnection.setReadTimeout(readTimeout);
            httpsUrlConnection.connect();
        } catch (IOException e) {
            throw e;
        } finally {
            httpsUrlConnection.disconnect();
        }
    }

    private int startProxy(ProxyRuleList rules) throws InterruptedException {
        int targetPort = webServer.getHttpsPort();

        ProxyConfig config = ProxyConfig.builder()
                .targetPort(targetPort)
                .build();

        proxy = new JProxy(config, rules);
        return proxy.start();
    }
}
