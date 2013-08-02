package fromjava.abstractimpl;

import javax.jws.WebService;

@WebService()
public class EchoImpl extends Echo {
    public Bar echoBar(Bar bar) {
        return bar;
    }

    public String echoString(String str) {
        return str;
    }
}
