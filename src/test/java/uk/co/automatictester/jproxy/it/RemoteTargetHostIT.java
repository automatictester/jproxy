package uk.co.automatictester.jproxy.it;

import io.restassured.config.RestAssuredConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.automatictester.jproxy.JProxy;
import uk.co.automatictester.jproxy.ProxyConfig;
import uk.co.automatictester.jproxy.ProxyRuleList;
import uk.co.automatictester.jproxy.util.RestAssuredConfigFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketException;

import static io.restassured.RestAssured.given;

public class RemoteTargetHostIT {

    private JProxy proxy;
    private int proxyPort;

    @Test
    public void testConnect() {
        makeHttpGetRequestOverTls();
    }

    @Test(dependsOnMethods = "testConnect", expectedExceptions = {SocketException.class, SSLHandshakeException.class, SSLException.class})
    public void testDisconnect() {
        makeHttpGetRequestOverTls();
    }

    @BeforeClass
    public void setup() throws InterruptedException {
        ProxyConfig config = ProxyConfig.builder()
                .targetHost("tls-v1-2.badssl.com")
                .targetPort(1012)
                .outboundConnectTimeout(5000)
                .outboundReadTimeout(8000)
                .inboundPoolTerminationTimeout(1000)
                .build();

        ProxyRuleList rules = new ProxyRuleList()
                .addConnect()
                .addDisconnect();

        proxy = new JProxy(config, rules);
        proxyPort = proxy.start();
    }

    @AfterClass
    public void cleanup() throws InterruptedException {
        proxy.stop();
    }

    private void makeHttpGetRequestOverTls() {
        RestAssuredConfig config = RestAssuredConfigFactory.getInstance();
        given().config(config)
                .baseUri("https://localhost")
                .port(proxyPort)
                .log().uri()
                .when().get()
                .then().log().all()
                .statusCode(200);
    }
}
