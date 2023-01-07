package uk.co.automatictester.jproxy.rule;

import lombok.Getter;

public abstract class DelayRule extends ProxyRule {

    @Getter
    protected final int delay;

    public DelayRule(int delay) {
        validateDelay(delay);
        this.delay = delay;
    }

    private void validateDelay(int delay) {
        if (delay < 1) throw new IllegalArgumentException("invalid delay: " + delay);
    }
}
