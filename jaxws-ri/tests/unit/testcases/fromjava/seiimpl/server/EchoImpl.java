
package fromjava.seiimpl.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import java.util.List;

@WebService(endpointInterface="fromjava.seiimpl.server.EchoIF")
public class EchoImpl implements EchoIF {
    public Bar echoBar(Bar bar) {
        return bar;
    }

    public String echoString(String str) {
        return str;
    }

    public String echoStringHolder(Holder<String> str) {
        return str.value;
    }

    public List<Bar> echoBarList(List<Bar> list) {
        return list;
    }

}
