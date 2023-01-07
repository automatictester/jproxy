package uk.co.automatictester.jproxy.rule;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class DelayConnect extends DelayRule {

    public DelayConnect(int delay) {
        super(delay);
    }
}
