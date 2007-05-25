package com.sun.xml.ws.transport.http;

/**
 * @author Jitendra Kotamraju
 *
 */
public class HttpDump implements HttpDumpMBean {
    public void setDump(boolean dump) {
        HttpAdapter.dump = dump;
    }

    public boolean getDump() {
        return HttpAdapter.dump;
    }
}
