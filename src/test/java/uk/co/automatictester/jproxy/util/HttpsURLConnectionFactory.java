package uk.co.automatictester.jproxy.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpsURLConnectionFactory {

    public static HttpsURLConnection createInstance(String url) {
        URLConnection urlConnection;
        try {
            URL serverUrl = new URL(url);
            urlConnection = serverUrl.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;
        SSLSocketFactory sslSocketFactory = TrustAllSSLSocketFactory.createInstance();
        httpsUrlConnection.setSSLSocketFactory(sslSocketFactory);
        // skip hostname verification
        httpsUrlConnection.setHostnameVerifier((hostname, sslSession) -> true);

        return httpsUrlConnection;
    }
}
