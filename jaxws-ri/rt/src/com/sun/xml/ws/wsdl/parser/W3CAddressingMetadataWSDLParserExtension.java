package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.api.model.wsdl.*;
import com.sun.xml.ws.model.wsdl.WSDLOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;

/**
 * W3C WS-Addressing Runtime WSDL parser extension that parses
 * WS-Addressing Metadata wsdl extensibility elements
 * This mainly reads wsam:Action element on input/output/fault messages in wsdl.
 *
 * @author Rama Pulavarthi
 */
public class W3CAddressingMetadataWSDLParserExtension extends W3CAddressingWSDLParserExtension {

    String METADATA_WSDL_EXTN_NS = "http://www.w3.org/2007/05/addressing/metadata";
    QName METADATA_WSDL_ACTION_TAG = new QName(METADATA_WSDL_EXTN_NS, "Action", "wsam");

    @Override
    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader) {
        return false;
    }

    @Override
    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        return false;
    }

    @Override
    public boolean bindingOperationElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return false;
    }

    @Override
    public boolean portTypeOperationInput(WSDLOperation o, XMLStreamReader reader) {
        WSDLOperationImpl impl = (WSDLOperationImpl) o;

        String action = ParserUtil.getAttribute(reader, METADATA_WSDL_ACTION_TAG);
        if (action != null) {
            impl.getInput().setAction(action);
            impl.getInput().setDefaultAction(false);
        }

        return false;
    }

    @Override
    public boolean portTypeOperationOutput(WSDLOperation o, XMLStreamReader reader) {
        WSDLOperationImpl impl = (WSDLOperationImpl) o;

        String action = ParserUtil.getAttribute(reader, METADATA_WSDL_ACTION_TAG);
        if (action != null) {
            impl.getOutput().setAction(action);
        }

        return false;
    }

    @Override
    public boolean portTypeOperationFault(WSDLOperation o, XMLStreamReader reader) {
        WSDLOperationImpl impl = (WSDLOperationImpl) o;

        String action = ParserUtil.getAttribute(reader, METADATA_WSDL_ACTION_TAG);
        if (action != null) {
            String name = ParserUtil.getMandatoryNonEmptyAttribute(reader, "name");
            impl.getFaultActionMap().put(name, action);
        }

        return false;
    }

    @Override
    protected void patchAnonymousDefault(WSDLBoundPortTypeImpl binding) {
    }

    @Override
    protected String getNamespaceURI() {
        return METADATA_WSDL_EXTN_NS;
    }
}
    