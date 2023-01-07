package uk.co.automatictester.jproxy.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DaemonThreadFactory {

    public static ThreadFactory getInstance() {
        return r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        };
    }
}
