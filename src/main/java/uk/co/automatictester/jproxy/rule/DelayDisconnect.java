package uk.co.automatictester.jproxy.rule;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class DelayDisconnect extends DelayRule {

    public DelayDisconnect(int delay) {
        super(delay);
    }
}
