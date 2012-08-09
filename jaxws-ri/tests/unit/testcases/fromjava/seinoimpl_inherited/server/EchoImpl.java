
package fromjava.seinoimpl_inherited.server;

import javax.jws.WebService;

@WebService(endpointInterface="fromjava.seinoimpl_inherited.server.EchoIF")
public class EchoImpl {
    public Bar echoBar(Bar bar) {
        return bar;
    }

    public String echoString(String str) {
        return str;
    }
}
