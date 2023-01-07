package uk.co.automatictester.jproxy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProxyConfig {

    int proxyPort;
    int targetPort;
    String targetHost;
    int outboundConnectTimeout;
    int outboundReadTimeout;
    int inboundPoolSize;
    int connectionQueueSize;
    int inboundPoolTerminationTimeout;
    int monitorFrequency;

    /**
     * Validates the config.
     *
     * @throws IllegalArgumentException if config is invalid.
     */
    public void validate() {
        if (targetPort == 0) {
            throw new IllegalArgumentException("targetPort must be set");
        }
        if (proxyPort == targetPort) {
            throw new IllegalArgumentException("proxyPort and targetPort must be different");
        }
    }

    public static class ProxyConfigBuilder {
        private int proxyPort = 0;
        private int targetPort;
        private String targetHost = "localhost";
        private int outboundConnectTimeout = 0;
        private int outboundReadTimeout = 0;
        private int inboundPoolSize = 10;
        private int connectionQueueSize = 0;
        private int inboundPoolTerminationTimeout = 2000;
        private int monitorFrequency = 0;

        /**
         * Sets proxy port.
         *
         * @param proxyPort port number to run the proxy on.
         * @return this builder.
         */
        public ProxyConfigBuilder proxyPort(int proxyPort) {
            if (!isValidPort(proxyPort)) {
                throw new IllegalArgumentException("invalid proxyPort: " + proxyPort);
            }
            this.proxyPort = proxyPort;
            return this;
        }

        /**
         * Sets target port.
         *
         * @param targetPort target port number to forward the traffic to.
         * @return this builder.
         */
        public ProxyConfigBuilder targetPort(int targetPort) {
            if (!isValidPort(targetPort)) {
                throw new IllegalArgumentException("invalid targetPort: " + targetPort);
            }
            this.targetPort = targetPort;
            return this;
        }

        /**
         * Sets target host.
         *
         * @param targetHost target host to forward the traffic to.
         * @return this builder.
         */
        public ProxyConfigBuilder targetHost(String targetHost) {
            if (!isNonEmpty(targetHost)) {
                throw new IllegalArgumentException("invalid targetHost: '" + targetHost + "'");
            }
            this.targetHost = targetHost;
            return this;
        }

        /**
         * Sets outbound connect timeout.
         *
         * @param outboundConnectTimeout connect timeout (in ms) for outbound connections between the proxy and the target.
         * @return this builder.
         */
        public ProxyConfigBuilder outboundConnectTimeout(int outboundConnectTimeout) {
            if (!isNonNegative(outboundConnectTimeout)) {
                throw new IllegalArgumentException("invalid outboundConnectTimeout: " + outboundConnectTimeout);
            }
            this.outboundConnectTimeout = outboundConnectTimeout;
            return this;
        }

        /**
         * Sets outbound read timeout.
         *
         * @param outboundReadTimeout read timeout (in ms) for outbound connections between the proxy and the target.
         * @return this builder.
         */
        public ProxyConfigBuilder outboundReadTimeout(int outboundReadTimeout) {
            if (!isNonNegative(outboundReadTimeout)) {
                throw new IllegalArgumentException("invalid outboundReadTimeout: " + outboundReadTimeout);
            }
            this.outboundReadTimeout = outboundReadTimeout;
            return this;
        }

        /**
         * Sets inbound pool size.
         *
         * @param inboundPoolSize thread pool size for handling inbound connections.
         * @return this builder.
         */
        public ProxyConfigBuilder inboundPoolSize(int inboundPoolSize) {
            if (!isPositive(inboundPoolSize)) {
                throw new IllegalArgumentException("invalid inboundPoolSize: " + inboundPoolSize);
            }
            this.inboundPoolSize = inboundPoolSize;
            return this;
        }

        /**
         * Sets connection queue size.
         *
         * @param connectionQueueSize Connection queue size.
         * @return this builder.
         */
        public ProxyConfigBuilder connectionQueueSize(int connectionQueueSize) {
            if (!isNonNegative(connectionQueueSize)) {
                throw new IllegalArgumentException("invalid connectionQueueSize: " + connectionQueueSize);
            }
            this.connectionQueueSize = connectionQueueSize;
            return this;
        }

        /**
         * Sets time to wait (in ms) for thread pool to terminate when stopping the proxy.
         *
         * @param inboundPoolTerminationTimeout time to wait (in ms) for thread pool to terminate when stopping the proxy.
         * @return this builder.
         */
        public ProxyConfigBuilder inboundPoolTerminationTimeout(int inboundPoolTerminationTimeout) {
            if (!isPositive(inboundPoolTerminationTimeout)) {
                throw new IllegalArgumentException("invalid inboundPoolTerminationTimeout: " + inboundPoolTerminationTimeout);
            }
            this.inboundPoolTerminationTimeout = inboundPoolTerminationTimeout;
            return this;
        }

        /**
         * Sets frequency (in ms) to monitor and log number of active inbound connections.
         *
         * @param monitorFrequency frequency (in ms) to monitor and log number of active inbound connections.
         * @return this builder.
         */
        public ProxyConfigBuilder monitorFrequency(int monitorFrequency) {
            if (monitorFrequency < 5000) {
                throw new IllegalArgumentException("invalid monitorFrequency: " + monitorFrequency);
            }
            this.monitorFrequency = monitorFrequency;
            return this;
        }

        private boolean isValidPort(int number) {
            return number > 0 && number <= 65535;
        }

        private boolean isNonNegative(int number) {
            return number >= 0;
        }

        private boolean isPositive(int number) {
            return number > 0;
        }

        private boolean isNonEmpty(String input) {
            return input != null && !input.isEmpty();
        }
    }
}
