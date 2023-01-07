package uk.co.automatictester.jproxy;

import uk.co.automatictester.jproxy.rule.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyRuleList {

    private final AtomicInteger index = new AtomicInteger(0);
    private final List<ProxyRule> rules;

    /**
     * Constructs an empty list.
     */
    public ProxyRuleList() {
        this.rules = new ArrayList<>();
    }

    /**
     * Constructs a list containing the rules of the specified list.
     */
    public ProxyRuleList(ProxyRuleList other) {
        this.rules = new ArrayList<>(other.rules);
    }

    /**
     * Add <b>Connect</b> rule to the list.
     *
     * @return this list.
     */
    public ProxyRuleList addConnect() {
        add(new Connect());
        return this;
    }

    /**
     * Add <b>Disconnect</b> rule to the list.
     *
     * @return this list.
     */
    public ProxyRuleList addDisconnect() {
        add(new Disconnect());
        return this;
    }

    /**
     * Add <b>DelayConnect</b> rule to the list.
     *
     * @param delay delay (in ms).
     * @return this list.
     */
    public ProxyRuleList addDelayConnect(int delay) {
        add(new DelayConnect(delay));
        return this;
    }

    /**
     * Add <b>DelayDisconnect</b> rule to the list.
     *
     * @param delay delay (in ms).
     * @return this list.
     */
    public ProxyRuleList addDelayDisconnect(int delay) {
        add(new DelayDisconnect(delay));
        return this;
    }

    /**
     * Add <b>Connect</b> rules to the list.
     *
     * @param count number of rules of this type to add.
     * @return this list.
     */
    public ProxyRuleList addConnect(int count) {
        validateCount(count);
        for (int i = 0; i < count; i++) {
            addConnect();
        }
        return this;
    }

    /**
     * Add <b>Disconnect</b> rules to the list.
     *
     * @param count number of rules of this type to add.
     * @return this list.
     */
    public ProxyRuleList addDisconnect(int count) {
        validateCount(count);
        for (int i = 0; i < count; i++) {
            addDisconnect();
        }
        return this;
    }

    /**
     * Add <b>DelayConnect</b> rules to the list.
     *
     * @param delay delay (in ms).
     * @param count number of rules of this type to add.
     * @return this list.
     */
    public ProxyRuleList addDelayConnect(int delay, int count) {
        validateCount(count);
        for (int i = 0; i < count; i++) {
            addDelayConnect(delay);
        }
        return this;
    }

    /**
     * Add <b>DelayDisconnect</b> rules to the list.
     *
     * @param delay delay (in ms).
     * @param count number of rules of this type to add.
     * @return this list.
     */
    public ProxyRuleList addDelayDisconnect(int delay, int count) {
        validateCount(count);
        for (int i = 0; i < count; i++) {
            addDelayDisconnect(delay);
        }
        return this;
    }

    private void add(ProxyRule rule) {
        rules.add(rule);
    }

    private void validateCount(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
    }

    /**
     * Returns the number of rules in this list.
     *
     * @return the number of rules in this list.
     */
    public int getSize() {
        return rules.size();
    }

    /**
     * Returns the next rule in this list.
     *
     * @return the next rule in this list.
     */
    public ProxyRule get() {
        int currentId = index.getAndIncrement();
        int ruleId = currentId % getSize();
        return rules.get(ruleId);
    }
}
