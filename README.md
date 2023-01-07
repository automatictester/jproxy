# JProxy

[![Central status](https://maven-badges.herokuapp.com/maven-central/uk.co.automatictester/jproxy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.co.automatictester/jproxy)
[![Java](https://github.com/automatictester/jproxy/actions/workflows/maven.yml/badge.svg)](https://github.com/automatictester/jproxy/actions/workflows/maven.yml)
[![CodeQL](https://github.com/automatictester/jproxy/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/automatictester/jproxy/actions/workflows/codeql-analysis.yml)

Java TCP proxy for simulating network conditions in integration testing.

## How it works

JProxy is a TCP proxy implemented as a Java library. Installation of any additional binaries or system packages is not
required.

#### Features

- Built for use in integration testing.
- Support for TCP-based protocols.
- Different, predictable handling for every single TCP connection.
- No interference with the connection before the TCP handshake is finished.
- No further interaction with the connection after applying given rule.

#### Design

JProxy design is based on blocking, multi-threaded architecture, where every incoming TCP connection is handled by
a dedicated thread. Threads are assigned from a fixed thread pool. Connection handling steps:

- TCP handshake is performed, and the connection is added to the accept queue.
- JProxy consumes the connection from the accept queue by performing `Socket inbound = serverSocket.accept()`.
- Connection is handled according to `ProxyRuleList`.

#### JProxy vs other tools

- For simulating faults per HTTP/HTTPS request, you might prefer to rely on features provided by popular HTTP/HTTPS
  mocking frameworks.
- For simulating network conditions in system level testing (ongoing latency, jitter or limited bandwidth), you
  might prefer to use different tools.

## Quick start guide

```
// configure the proxy
ProxyRuleList rules = new ProxyRuleList()
    .addConnect();

ProxyConfig config = ProxyConfig
    .builder()
    .targetPort(8080)
    .outboundReadTimeout(1000)
    .build();

JProxy proxy = new JProxy(config, rules);
int proxyPort = proxy.start();

// use the proxy
String httpUrl = String.format("http://localhost:%d/return-200", proxyPort);
URL url = new URL(httpUrl);
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setReadTimeout(1000);
connection.connect();
int responseCode = connection.getResponseCode();
connection.disconnect();
assertThat(responseCode).isEqualTo(200);

// stop the proxy
proxy.stop();
```

## Configuration

#### Proxy configuration

```
ProxyConfig config = ProxyConfig.builder()
    .proxyPort(proxyPort)
    .targetHost(targetHost)
    .targetPort(targetPort)
    .connectionQueueSize(connectionQueueSize)
    .inboundPoolSize(inboundPoolSize)
    .inboundPoolTerminationTimeout(inboundPoolTerminationTimeout)
    .outboundConnectTimeout(outboundConnectTimeout)
    .outboundReadTimeout(outboundReadTimeout)
    .monitorFrequency(monitorFrequency)
    .build();
```

| Parameter                     | Description                                                                                                    | 
|-------------------------------|----------------------------------------------------------------------------------------------------------------|
| proxyPort                     | Port number to run the proxy on. Defaults to 0 - use dynamically allocated free port.                          |
| targetHost                    | Target host to forward the traffic to. Defaults to localhost.                                                  |
| targetPort                    | Target port number to forward the traffic to. Required.                                                        |
| connectionQueueSize           | Connection queue size. Defaults to 0 - use the Java default.                                                   |
| inboundPoolSize               | Thread pool size for handling inbound connections. Defaults to 10.                                             |
| inboundPoolTerminationTimeout | Time to wait (in ms) for thread pool to terminate when stopping the proxy. Defaults to 2000.                   |
| outboundConnectTimeout        | Connect timeout (in ms) for outbound connections between the proxy and the target. Defaults to 0 - no timeout. |
| outboundReadTimeout           | Read timeout (in ms) for outbound connections between the proxy and the target. Defaults to 0 - no timeout.    |
| monitorFrequency              | Frequency (in ms) to monitor and log number of active inbound connections. Defaults to 0 - disabled.           |

#### Connection handling rules

```
ProxyRuleList rules = new ProxyRuleList()
    .addConnect()
    .addDisconnect()
    .addDelayConnect(timeout)
    .addDelayDisconnect(timeout);
```

Connection handling rules are applied to inbound connections in order they are defined.
If there are more connections than connection handling rules, rule list wraps around.

| Connection handling rule | Description                                                             |
|--------------------------|-------------------------------------------------------------------------|
| Connect                  | Forward the connection to the configured target.                        |
| Disconnect               | Close the connection.                                                   |
| DelayConnect             | Forward the connection to the configured target after configured delay. |
| DelayDisconnect          | Close the connection after configured delay.                            |

## Tips

#### VPN software

Certain VPN software, in particular with Kill Switch enabled, might be interfering with JProxy.

#### HTTP connection reuse

If you send HTTP traffic through JProxy, established TCP connections may remain open even when idle. Depending on the
usage pattern this might cause the proxy to run out of available threads to handle new inbound connections. This might
be mitigated in a number of ways:

- Configure your HTTP client to close the connections when no longer needed.
- Configure `inboundPoolSize` to increase the inbound thread pool size.
- Configure `outboundReadTimeout` to close the connections when idle.

#### Different exceptions on connection failures

It should be assumed that the code under test might need to handle more than one class of exception thrown on
connection failures. The exact exception will depend on how the network failure is handled by the underlying library,
connection failure mode and the exact stage of establishing a connection.

Below are sample exceptions thrown by `HttpsURLConnection.connect()` on `Disconnect` rule across different JDK versions:

- `java.net.SocketException: Connection reset`
- `javax.net.ssl.SSLException: Connection reset`
- `javax.net.ssl.SSLHandshakeException: Remote host closed connection during handshake`
- `javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake`

## Supported Java versions

Truststore Maven Plugin is tested against Java LTS versions 8, 11 and 17.