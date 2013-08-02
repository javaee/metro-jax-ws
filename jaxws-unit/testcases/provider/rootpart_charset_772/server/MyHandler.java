package provider.rootpart_charset_772.server;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.util.Set;

/**
 * Handler adds an attachment on the outbound direction.
 *
 * @author Jitendra Kotamraju
 */
public class MyHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        if (!(Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))
            return true;
        try {
            SOAPMessage msg = smc.getMessage();
            AttachmentPart part = msg.createAttachmentPart(getDataHandler());
            part.setContentId("SOAPTestHandler@example.jaxws.sun.com");
            msg.addAttachmentPart(part);
            msg.saveChanges();
            smc.setMessage(msg);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {}
    
    private DataHandler getDataHandler() throws Exception {
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return "text/xml";
            }

            public InputStream getInputStream() {
                return new ByteArrayInputStream("<a/>".getBytes());
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        });
    }

}
