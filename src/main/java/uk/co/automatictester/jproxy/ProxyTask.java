package uk.co.automatictester.jproxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.automatictester.jproxy.handler.ConnectHandler;
import uk.co.automatictester.jproxy.handler.DelayHandler;
import uk.co.automatictester.jproxy.handler.DisconnectHandler;
import uk.co.automatictester.jproxy.handler.Request;
import uk.co.automatictester.jproxy.rule.ProxyRule;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
class ProxyTask implements Callable<Void> {

    private final Socket inboundSocket;
    private final int connectionId;
    private final ProxyRule rule;
    private final ProxyConfig config;

    @Override
    public Void call() {
        try {
            Request request = new Request(rule, inboundSocket, connectionId);

            DelayHandler delayHandler = new DelayHandler();
            ConnectHandler connectHandler = new ConnectHandler(
                    config.getTargetHost(), config.getTargetPort(),
                    config.getOutboundConnectTimeout(), config.getOutboundReadTimeout()
            );
            DisconnectHandler disconnectHandler = new DisconnectHandler();

            delayHandler.setNextHandler(connectHandler);
            connectHandler.setNextHandler(disconnectHandler);

            delayHandler.handleRequest(request);
        } catch (IOException e) {
            log.error("Connection {} processing error: {}", connectionId, e.getMessage());
        } finally {
            if (inboundSocket != null) {
                try {
                    log.info("Connection {} closing", connectionId);
                    inboundSocket.close();
                } catch (IOException e) {
                    log.error("Connection {} closing error: {}", connectionId, e.getMessage());
                }
            }
        }
        return null;
    }
}
