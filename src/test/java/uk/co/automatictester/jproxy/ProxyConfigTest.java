package uk.co.automatictester.jproxy;

import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ProxyConfigTest {

    @Test
    public void testDefault() {
        ProxyConfig config = ProxyConfig.builder().build();
        assertThat(config.getProxyPort()).isEqualTo(0);
        assertThat(config.getTargetPort()).isEqualTo(0);
        assertThat(config.getTargetHost()).isEqualTo("localhost");
        assertThat(config.getOutboundConnectTimeout()).isEqualTo(0);
        assertThat(config.getOutboundReadTimeout()).isEqualTo(0);
        assertThat(config.getInboundPoolSize()).isEqualTo(10);
        assertThat(config.getConnectionQueueSize()).isEqualTo(0);
        assertThat(config.getInboundPoolTerminationTimeout()).isEqualTo(2000);
        assertThat(config.getMonitorFrequency()).isEqualTo(0);
    }

    @Test
    public void testCustom() {
        int proxyPort = 8080;
        int targetPort = 8081;
        String targetHost = "127.0.0.1";
        int outboundConnectTimeout = 5_000;
        int outboundReadTimeout = 10_000;
        int inboundPoolSize = 20;
        int connectionQueueSize = 1;
        int inboundPoolTerminationTimeout = 10000;
        int monitorFrequency = 5000;

        ProxyConfig config = ProxyConfig.builder()
                .proxyPort(proxyPort)
                .targetPort(targetPort)
                .targetHost(targetHost)
                .outboundConnectTimeout(outboundConnectTimeout)
                .outboundReadTimeout(outboundReadTimeout)
                .inboundPoolSize(inboundPoolSize)
                .connectionQueueSize(connectionQueueSize)
                .inboundPoolTerminationTimeout(inboundPoolTerminationTimeout)
                .monitorFrequency(monitorFrequency)
                .build();

        assertThat(config.getProxyPort()).isEqualTo(proxyPort);
        assertThat(config.getTargetPort()).isEqualTo(targetPort);
        assertThat(config.getTargetHost()).isEqualTo(targetHost);
        assertThat(config.getOutboundConnectTimeout()).isEqualTo(outboundConnectTimeout);
        assertThat(config.getOutboundReadTimeout()).isEqualTo(outboundReadTimeout);
        assertThat(config.getInboundPoolSize()).isEqualTo(inboundPoolSize);
        assertThat(config.getConnectionQueueSize()).isEqualTo(connectionQueueSize);
        assertThat(config.getInboundPoolTerminationTimeout()).isEqualTo(inboundPoolTerminationTimeout);
        assertThat(config.getMonitorFrequency()).isEqualTo(monitorFrequency);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNoTargetPort() {
        ProxyConfig.builder().build().validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testProxyPortAndTargetPortTheSame() {
        int port = 8081;
        ProxyConfig.builder()
                .proxyPort(port)
                .targetPort(port)
                .build().validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testPortNegative() {
        ProxyConfig.builder()
                .proxyPort(-1)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testPortTooLarge() {
        ProxyConfig.builder()
                .proxyPort(65536)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTargetPortNegative() {
        ProxyConfig.builder()
                .targetPort(-1)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTargetPortTooLarge() {
        ProxyConfig.builder()
                .targetPort(65536)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTargetHostNull() {
        ProxyConfig.builder()
                .targetHost(null)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTargetHostEmpty() {
        ProxyConfig.builder()
                .targetHost("")
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOutboundConnectTimeoutNegative() {
        ProxyConfig.builder()
                .outboundConnectTimeout(-1)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOutboundReadTimeoutNegative() {
        ProxyConfig.builder()
                .outboundReadTimeout(-1)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testThreadZero() {
        ProxyConfig.builder()
                .inboundPoolSize(0)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConnectionQueueSizeNegative() {
        ProxyConfig.builder()
                .connectionQueueSize(-1)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEexecutorServiceTerminationTimeoutNegative() {
        ProxyConfig.builder()
                .inboundPoolTerminationTimeout(-1)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMonitorFrequencyNegative() {
        ProxyConfig.builder()
                .monitorFrequency(4999)
                .build();
    }
}
