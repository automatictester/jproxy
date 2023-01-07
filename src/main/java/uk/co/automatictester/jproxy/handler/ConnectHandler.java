package uk.co.automatictester.jproxy.handler;

import lombok.extern.slf4j.Slf4j;
import uk.co.automatictester.jproxy.rule.Connect;
import uk.co.automatictester.jproxy.rule.DelayConnect;
import uk.co.automatictester.jproxy.rule.ProxyRule;
import uk.co.automatictester.jproxy.util.DaemonThreadFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class ConnectHandler implements Handler {

    private final ExecutorService outboundPool;
    private final String host;
    private final int targetPort;
    private final int outboundConnectTimeout;
    private final int outboundReadTimeout;
    private Handler next;

    public ConnectHandler(String host, int targetPort, int outboundConnectTimeout, int outboundReadTimeout) {
        ThreadFactory daemonThreadFactory = DaemonThreadFactory.getInstance();
        this.host = host;
        this.targetPort = targetPort;
        this.outboundConnectTimeout = outboundConnectTimeout;
        this.outboundReadTimeout = outboundReadTimeout;
        this.outboundPool = Executors.newCachedThreadPool(daemonThreadFactory);
    }

    @Override
    public void handleRequest(Request request) throws IOException {
        ProxyRule rule = request.getProxyRule();
        if (rule instanceof Connect || rule instanceof DelayConnect) {
            try (Socket outbound = new Socket()) {
                int cid = request.getCid();
                log.info("Connection {} allowed", cid);

                Socket inbound = request.getInbound();
                InputStream clientIn = inbound.getInputStream();
                OutputStream clientOut = inbound.getOutputStream();

                SocketAddress address = new InetSocketAddress(host, targetPort);
                outbound.setSoTimeout(outboundReadTimeout);
                outbound.connect(address, outboundConnectTimeout);
                InputStream serverIn = outbound.getInputStream();
                OutputStream serverOut = outbound.getOutputStream();
                Callable<Void> processOutboundRequests = () -> {
                    transferStream(clientIn, serverOut);
                    return null;
                };
                outboundPool.submit(processOutboundRequests);
                sendResponseToClient(serverIn, clientOut, cid);
            }
        } else if (next != null) {
            next.handleRequest(request);
        }
    }

    private void sendResponseToClient(InputStream serverIn, OutputStream clientOut, int cid) throws IOException {
        try {
            transferStream(serverIn, clientOut);
        } catch (SocketException e) {
            log.info("Connection {} could not send response back to client: {}", cid, e.getMessage());
        } catch (SocketTimeoutException e) {
            log.info("Connection {} outbound: {}", cid, e.getMessage());
        }
    }

    private void transferStream(InputStream serverIn, OutputStream clientOut) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = serverIn.read(buffer)) != -1) {
            clientOut.write(buffer, 0, len);
        }
    }

    @Override
    public void setNextHandler(Handler handler) {
        next = handler;
    }
}
