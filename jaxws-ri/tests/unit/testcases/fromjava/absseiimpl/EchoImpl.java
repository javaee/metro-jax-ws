
package fromjava.absseiimpl;

import javax.jws.WebService;

@WebService(endpointInterface="fromjava.absseiimpl.EchoIF", portName="EchoPort")
public class EchoImpl implements EchoIF {
    public Bar echoBar(Bar bar) {
        return bar;
    }

    public String echoString(String str) {
        return str;
    }
}
