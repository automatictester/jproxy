package uk.co.automatictester.jproxy.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WebServer {

    private final WireMockServer server;

    public WebServer() {
        WireMockConfiguration config = options()
                .keystorePath("src/test/resources/wiremock-key-cert.p12")
                .keystorePassword("password")
                .dynamicHttpsPort();

        server = new WireMockServer(config);
        server.start();

        stubFor(get("/return-200").willReturn(aResponse().withStatus(200)));
    }

    public int getHttpsPort() {
        return server.httpsPort();
    }

    public void stop() {
        server.stop();
    }
}
