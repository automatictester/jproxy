package uk.co.automatictester.jproxy.load.simulation;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import uk.co.automatictester.jproxy.ProxyRuleList;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class DisconnectSimulation extends ProxySimulation {

    private final HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:" + proxyPort);
    private final ScenarioBuilder scenario = scenario("newConnections")
            .exec(http("newConnections").get("/return-200"));

    {
        setUp(scenario.injectOpen(constantUsersPerSec(5).during(10)))
                .maxDuration(60)
                .protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().between(88.0, 92.0),
                        global().responseTime().percentile3().lte(200),
                        global().failedRequests().percent().between(8.0, 12.0)
                );
    }

    @Override
    protected ProxyRuleList getRules() {
        return new ProxyRuleList()
                .addConnect(9)
                .addDisconnect(1);
    }
}
