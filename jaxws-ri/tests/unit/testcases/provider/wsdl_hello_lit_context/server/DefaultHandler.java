/**
 * $Id: DefaultHandler.java,v 1.1 2007-08-11 01:01:58 jitu Exp $
 */
package provider.wsdl_hello_lit_context.server;

import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Jitendra Kotamraju
 */
public class DefaultHandler
    implements LogicalHandler<LogicalMessageContext> {
    
    public boolean handleMessage(LogicalMessageContext messageContext) {
        Boolean bool = (Boolean)messageContext.get(
                MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!bool.booleanValue()) {
            // Do this only for Request
            LogicalMessage msg = messageContext.getMessage();
            String content = "<MyVoidTest xmlns=\"urn:test:types\"></MyVoidTest>";
            Source source = new StreamSource(
                new ByteArrayInputStream(content.getBytes()));
            msg.setPayload(source);
            messageContext.put("foo", "bar");
            messageContext.setScope("foo", MessageContext.Scope.APPLICATION);
        } else {
            LogicalMessage msg = messageContext.getMessage();
            msg.getPayload();
            String value = (String)messageContext.get("foo");
            if (value == null || !value.equals("return-bar")) {
                throw new IllegalArgumentException(
                    "Got foo property: expected=return-bar Got="+value);
            }
            value = (String)messageContext.get("return-foo");
            if (value == null || !value.equals("return-bar")) {
                throw new IllegalArgumentException(
                    "Got return-foo property: expected=return-bar Got="+value);
            }
        }
        return true;
    }
    
    public void close(MessageContext messageContext) {
    }    
    
    public boolean handleFault(LogicalMessageContext messageContext) {
        return true;
    }
    
}
