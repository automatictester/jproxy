package uk.co.automatictester.jproxy.util.apache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoRetryHttpClientFactory {

    public static HttpClient getInstance() {
        SystemDefaultHttpClient httpClient = new SystemDefaultHttpClient();
        httpClient.setHttpRequestRetryHandler((e, i, httpContext) -> false);
        return httpClient;
    }
}
