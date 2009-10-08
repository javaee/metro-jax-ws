package provider.attachment_595.client;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.util.Set;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Jitendra Kotamraju
 */
public class SOAPTestHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        boolean outbound = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outbound)
            return true;
        try {
            SOAPMessage msg = smc.getMessage();
            AttachmentPart part = msg.createAttachmentPart(getDataHandler("gpsXml.xml"));
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
    

    private DataHandler getDataHandler(final String file) throws Exception {
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return "text/xml";
            }

            public InputStream getInputStream() {
                return getClass().getClassLoader().getResourceAsStream(file);
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
