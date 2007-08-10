package fromjava.generics;

import javax.jws.WebService;

/**
 * Web Service uses generics.
 */
@WebService(endpointInterface="fromjava.generics.EchoIF", portName="EchoPort")
public class EchoImpl implements EchoIF<Bar> {
    public Bar echoBar(Bar bar) {
        return bar;
    }

    public String echoString(String str) {
        return str;
    }
}
