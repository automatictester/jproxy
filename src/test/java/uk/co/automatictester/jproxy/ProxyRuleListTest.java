package uk.co.automatictester.jproxy;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import uk.co.automatictester.jproxy.rule.*;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ProxyRuleListTest {

    @Test
    public void testGetLoop() {
        ProxyRuleList rules = new ProxyRuleList();
        rules.addConnect()
                .addDisconnect()
                .addDelayConnect(1000)
                .addDelayDisconnect(1000);

        for (int i = 0; i < 2; i++) {
            assertThat(rules.get()).isEqualTo(new Connect());
            assertThat(rules.get()).isEqualTo(new Disconnect());
            assertThat(rules.get()).isEqualTo(new DelayConnect(1000));
            assertThat(rules.get()).isEqualTo(new DelayDisconnect(1000));
        }
    }

    @Test
    public void testAddMultiple() {
        int connectCount = 7;
        int disconnectCount = 3;
        int connectCountAgain = 5;

        ProxyRuleList rules = new ProxyRuleList();
        rules.addConnect(connectCount)
                .addDisconnect(disconnectCount)
                .addConnect(connectCountAgain);

        for (int i = 0; i < connectCount; i++) {
            assertThat(rules.get()).isEqualTo(new Connect());
        }

        for (int i = 0; i < disconnectCount; i++) {
            assertThat(rules.get()).isEqualTo(new Disconnect());
        }

        for (int i = 0; i < connectCountAgain; i++) {
            assertThat(rules.get()).isEqualTo(new Connect());
        }
    }

    @Test
    public void testConcurrentGet() throws InterruptedException {
        ProxyRuleList rules = new ProxyRuleList();
        rules.addConnect()
                .addDisconnect()
                .addDelayConnect(100)
                .addDelayDisconnect(500);

        int threads = 4;
        int iterations = 100_000;

        CyclicBarrier barrier = new CyclicBarrier(threads);
        ConcurrentMap<ProxyRule, Integer> map = new ConcurrentHashMap<>();
        map.put(new Connect(), 0);
        map.put(new Disconnect(), 0);
        map.put(new DelayConnect(100), 0);
        map.put(new DelayDisconnect(500), 0);

        Runnable runnable = () -> {
            try {
                barrier.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < iterations; i++) {
                ProxyRule proxyRule = rules.get();
                map.computeIfPresent(proxyRule, (k, v) -> v + 1);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(runnable);
        }

        executor.shutdown();
        boolean gracefulShutdown = executor.awaitTermination(2, TimeUnit.SECONDS);
        if (!gracefulShutdown) {
            log.warn("Timeout elapsed before task termination, assertion failure expected");
        }

        int expectedCount = (threads / rules.getSize()) * iterations;
        log.info(map.toString());

        assertThat(map.size()).isEqualTo(4);
        assertThat(map.get(new Connect())).isEqualTo(expectedCount);
        assertThat(map.get(new Disconnect())).isEqualTo(expectedCount);
        assertThat(map.get(new DelayConnect(100))).isEqualTo(expectedCount);
        assertThat(map.get(new DelayDisconnect(500))).isEqualTo(expectedCount);
    }

    @Test
    public void testConcurrentGetDifferentPercent() throws InterruptedException {
        ProxyRuleList rules = new ProxyRuleList();
        rules.addConnect(80)
                .addDisconnect(5)
                .addDelayConnect(1000, 12)
                .addDelayDisconnect(1000, 3);

        int threads = 10;
        int iterations = 100_000;

        CyclicBarrier barrier = new CyclicBarrier(threads);
        ConcurrentMap<ProxyRule, Integer> map = new ConcurrentHashMap<>();
        map.put(new Connect(), 0);
        map.put(new Disconnect(), 0);
        map.put(new DelayConnect(1000), 0);
        map.put(new DelayDisconnect(2000), 0);

        Runnable runnable = () -> {
            try {
                barrier.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < iterations; i++) {
                ProxyRule proxyRule = rules.get();
                map.computeIfPresent(proxyRule, (k, v) -> v + 1);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(runnable);
        }

        executor.shutdown();
        boolean gracefulShutdown = executor.awaitTermination(2, TimeUnit.SECONDS);
        if (!gracefulShutdown) {
            log.warn("Timeout elapsed before task termination, assertion failure expected");
        }

        log.info(map.toString());

        assertThat(map.size()).isEqualTo(4);
        assertThat(map.get(new Connect())).isEqualTo(800_000);
        assertThat(map.get(new Disconnect())).isEqualTo(50_000);
        assertThat(map.get(new DelayConnect(1000))).isEqualTo(120_000);
        assertThat(map.get(new DelayDisconnect(2000))).isEqualTo(30_000);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddZeroCount() {
        ProxyRuleList rules = new ProxyRuleList();
        rules.addConnect(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNegativeCount() {
        ProxyRuleList rules = new ProxyRuleList();
        rules.addDelayConnect(-1);
    }
}
