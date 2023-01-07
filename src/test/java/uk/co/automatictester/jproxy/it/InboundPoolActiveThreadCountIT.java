package uk.co.automatictester.jproxy.it;

import io.restassured.config.RestAssuredConfig;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.automatictester.jproxy.JProxy;
import uk.co.automatictester.jproxy.ProxyConfig;
import uk.co.automatictester.jproxy.ProxyRuleList;
import uk.co.automatictester.jproxy.util.RestAssuredConfigFactory;
import uk.co.automatictester.jproxy.util.WebServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class InboundPoolActiveThreadCountIT {

    private final RestAssuredConfig config = RestAssuredConfigFactory.getInstance();
    private JProxy proxy;
    private WebServer webServer;

    @Test
    public void testInboundPoolActiveThreadCount() throws InterruptedException {
        int threadCount = 6;
        int proxyPort = startProxy(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        ((ThreadPoolExecutor) executor).prestartAllCoreThreads();

        List<Callable<Void>> connections = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            connections.add(() -> {
                makeHttpGetRequestOverTls(proxyPort);
                return null;
            });
        }

        executor.invokeAll(connections);
        assertThat(proxy.getInboundPoolActiveThreadCount()).isEqualTo(threadCount);

        Thread.sleep(2100);
        executor.invokeAll(connections.subList(0, threadCount / 2));
        assertThat(proxy.getInboundPoolActiveThreadCount()).isEqualTo(threadCount / 2);

        Thread.sleep(2100);
        makeHttpGetRequestOverTls(proxyPort);
        assertThat(proxy.getInboundPoolActiveThreadCount()).isEqualTo(1);

        Thread.sleep(2100);
        assertThat(proxy.getInboundPoolActiveThreadCount()).isEqualTo(0);
    }

    @BeforeClass
    public void setup() {
        webServer = new WebServer();
    }

    @AfterClass
    public void cleanup() throws InterruptedException {
        proxy.stop();
        webServer.stop();
    }

    private int startProxy(int inboundPoolSize) throws InterruptedException {
        int targetPort = webServer.getHttpsPort();

        ProxyConfig config = ProxyConfig.builder()
                .targetPort(targetPort)
                .inboundPoolSize(inboundPoolSize)
                .outboundReadTimeout(2000)
                .build();

        ProxyRuleList rules = new ProxyRuleList().addConnect();

        proxy = new JProxy(config, rules);
        return proxy.start();
    }

    private void makeHttpGetRequestOverTls(int proxyPort) {
        given().config(config)
                .baseUri("https://localhost")
                .port(proxyPort)
                .log().uri()
                .when().get("/return-200")
                .then().log().status()
                .statusCode(200);
    }
}
