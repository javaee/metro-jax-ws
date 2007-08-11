
package fromjava.seinoimpl.server;

import javax.jws.*;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import javax.annotation.Resource;

@WebService(endpointInterface="fromjava.seinoimpl.server.EchoIF")
public class EchoImpl  {
    public Bar echoBar(Bar bar) {
        return bar;
    }

    public String echoString(String str) {
/*
        MessageContext msgContext = wsContext.getMessageContext();
        QName opName = (QName)msgContext.get(MessageContext.WSDL_OPERATION);
        QName expected = new QName("http://example.org/", "echoString");
        if (!expected.equals(opName)) {
            throw new WebServiceException("Expected="+expected+
                " didn't match with received one="+opName);
        }
*/
        return str;
    }

    public void echoIntHolder(Holder<Integer> age) {
        age.value = age.value*2;
    }

    @Resource
    private WebServiceContext wsContext;
}
