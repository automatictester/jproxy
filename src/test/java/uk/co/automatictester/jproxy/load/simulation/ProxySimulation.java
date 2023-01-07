package uk.co.automatictester.jproxy.load.simulation;

import io.gatling.javaapi.core.Simulation;
import uk.co.automatictester.jproxy.JProxy;
import uk.co.automatictester.jproxy.ProxyConfig;
import uk.co.automatictester.jproxy.ProxyRuleList;
import uk.co.automatictester.jproxy.util.WebServer;

public abstract class ProxySimulation extends Simulation {

    protected final int proxyPort = 8081;
    private final WebServer webServer = new WebServer();
    private JProxy proxy;

    @Override
    public void before() {
        ProxyConfig config = ProxyConfig.builder()
                .proxyPort(proxyPort)
                .targetPort(8080)
                .outboundConnectTimeout(500)
                .outboundReadTimeout(2000)
                .monitorFrequency(5000)
                .build();

        ProxyRuleList rules = getRules();

        proxy = new JProxy(config, rules);

        try {
            proxy.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void after() {
        webServer.stop();
        try {
            proxy.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract ProxyRuleList getRules();
}
