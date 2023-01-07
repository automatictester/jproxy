package uk.co.automatictester.jproxy.util;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.co.automatictester.jproxy.util.apache.NoRetryHttpClientFactory;

import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.SSLConfig.sslConfig;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestAssuredConfigFactory {

    private static final HttpClientConfig.HttpClientFactory NO_RETRY_HTTP_CLIENT_FACTORY = NoRetryHttpClientFactory::getInstance;

    public static RestAssuredConfig getInstance() {
        return RestAssuredConfig.config()
                .httpClient(httpClientConfig().httpClientFactory(NO_RETRY_HTTP_CLIENT_FACTORY))
                .sslConfig(sslConfig().relaxedHTTPSValidation());
    }
}
