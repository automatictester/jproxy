package uk.co.automatictester.jproxy.handler;

import lombok.extern.slf4j.Slf4j;
import uk.co.automatictester.jproxy.rule.DelayDisconnect;
import uk.co.automatictester.jproxy.rule.Disconnect;
import uk.co.automatictester.jproxy.rule.ProxyRule;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class DisconnectHandler implements Handler {

    private Handler next;

    @Override
    public void handleRequest(Request request) throws IOException {
        ProxyRule rule = request.getProxyRule();
        if (rule instanceof Disconnect || rule instanceof DelayDisconnect) {
            int cid = request.getCid();
            log.info("Connection {} closed", cid);
            Socket inbound = request.getInbound();
            inbound.close();
        } else if (next != null) {
            next.handleRequest(request);
        }
    }

    @Override
    public void setNextHandler(Handler handler) {
        next = handler;
    }
}
