package uk.co.automatictester.jproxy.handler;

import lombok.AllArgsConstructor;
import lombok.Value;
import uk.co.automatictester.jproxy.rule.ProxyRule;

import java.net.Socket;

@Value
@AllArgsConstructor
public class Request {
    ProxyRule proxyRule;
    Socket inbound;
    int cid;
}
