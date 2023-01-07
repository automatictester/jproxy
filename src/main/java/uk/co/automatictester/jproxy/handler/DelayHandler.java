package uk.co.automatictester.jproxy.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.automatictester.jproxy.rule.DelayConnect;
import uk.co.automatictester.jproxy.rule.DelayDisconnect;
import uk.co.automatictester.jproxy.rule.DelayRule;
import uk.co.automatictester.jproxy.rule.ProxyRule;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class DelayHandler implements Handler {

    private Handler next;

    @Override
    public void handleRequest(Request request) throws IOException {
        ProxyRule rule = request.getProxyRule();
        if (rule instanceof DelayConnect || rule instanceof DelayDisconnect) {
            int cid = request.getCid();
            int delay = ((DelayRule) request.getProxyRule()).getDelay();
            log.info("Connection {} delayed by {}ms", cid, delay);
            sleep(delay);
        }
        if (next != null) {
            next.handleRequest(request);
        }
    }

    @Override
    public void setNextHandler(Handler handler) {
        next = handler;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
