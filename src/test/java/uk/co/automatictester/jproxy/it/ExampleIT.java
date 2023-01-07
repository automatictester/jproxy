package uk.co.automatictester.jproxy.it;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.automatictester.jproxy.JProxy;
import uk.co.automatictester.jproxy.ProxyConfig;
import uk.co.automatictester.jproxy.ProxyRuleList;
import uk.co.automatictester.jproxy.util.WebServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ExampleIT {

    private WebServer webServer;

    @Test
    public void testExample() throws InterruptedException, IOException {
        // configure the proxy
        ProxyRuleList rules = new ProxyRuleList()
                .addConnect();

        ProxyConfig config = ProxyConfig
                .builder()
                .targetPort(8080)
                .outboundReadTimeout(1000)
                .build();

        JProxy proxy = new JProxy(config, rules);
        int proxyPort = proxy.start();

        // use the proxy
        String httpUrl = String.format("http://localhost:%d/return-200", proxyPort);
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(1000);
        connection.connect();
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        assertThat(responseCode).isEqualTo(200);

        // stop the proxy
        proxy.stop();
    }

    @BeforeClass
    public void setup() {
        webServer = new WebServer();
    }

    @AfterClass
    public void cleanup() {
        webServer.stop();
    }
}
