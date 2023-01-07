package uk.co.automatictester.jproxy.handler;

import java.io.IOException;

public interface Handler {
    void handleRequest(Request request) throws IOException;

    void setNextHandler(Handler handler);
}
