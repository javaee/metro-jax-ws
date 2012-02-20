package fromwsdl.handler_simple.client;

import org.w3c.dom.Element;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Set;

import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.addressing.W3CAddressingConstants;

/**
 * This handler will be set on the soap 12 binding in the
 * customization file. It's used to test that bindings with multiple
 * ports actually use the correct ports. See bug 6353179 and
 * the HandlerClient tests cases.
 */
public class ReferenceParameterHandler implements SOAPHandler<SOAPMessageContext> {
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if(!outbound) {
            return true;
        }

        List<Element> refParams = (List<Element>) context.get(MessageContext.REFERENCE_PARAMETERS);
        System.out.println(refParams.size());
        if (refParams.size() != 2) {
            throw new WebServiceException("Did n't get expected ReferenceParameters");
        }
        try {
            for (Element e : refParams) {
            	XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
            	writer.writeStartDocument();
                DOMUtil.serializeNode(e, writer);
                if (e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME, "IsReferenceParameter") == null)
                    throw new WebServiceException("isReferenceParameter attribute not present on header");
            	writer.writeEndDocument();
            	writer.close();
            }
        } catch (XMLStreamException el) {
            throw new WebServiceException(el);
        }

        return true;
    }

    /**** empty methods below ****/
    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext context) {}

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

}
