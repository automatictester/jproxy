package uk.co.automatictester.jproxy.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrustAllSSLSocketFactory {

    public static SSLSocketFactory createInstance() {
        TrustManager[] trustManagers = TrustManagersFactory.createInstance();
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return sslContext.getSocketFactory();
    }
}
