package uk.co.automatictester.jproxy.load.simulation;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import uk.co.automatictester.jproxy.ProxyRuleList;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ReuseConnectionsSimulation extends ProxySimulation {

    private final HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:" + proxyPort);
    private final ScenarioBuilder scenario = scenario("reuseConnections")
            .during(10)
            .on(exec(http("reuseConnections").get("/return-200"))
                    .pause(1)
            );

    {
        setUp(scenario.injectClosed(constantConcurrentUsers(10).during(10)))
                .maxDuration(60)
                .protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().is(100.0),
                        global().responseTime().percentile3().lte(200)
                );
    }

    @Override
    protected ProxyRuleList getRules() {
        return new ProxyRuleList().addConnect();
    }
}
