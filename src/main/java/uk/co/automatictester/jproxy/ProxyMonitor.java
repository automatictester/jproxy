package uk.co.automatictester.jproxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ProxyMonitor implements Runnable {

    private final JProxy proxy;
    private final int monitorFrequency;

    @Override
    public void run() {
        while (true) {
            sleep(monitorFrequency);
            try {
                log.info("Active threads in inbound pool: {}", proxy.getInboundPoolActiveThreadCount());
            } catch (IllegalStateException e) {
                // exception will be thrown after shutting down the pool
            }
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // exception will be thrown on shutting down the pool
        }
    }
}
