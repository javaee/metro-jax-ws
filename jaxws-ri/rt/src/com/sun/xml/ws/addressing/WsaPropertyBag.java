package com.sun.xml.ws.addressing;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.PropertySet;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.developer.JAXWSProperties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Provides access to the Addressing headers.
 *
 * @author Kohsuke Kawaguchi
 * @author Rama Pulavarthi
 * @since 2.1.3
 */
public class WsaPropertyBag extends PropertySet {
    private final @NotNull AddressingVersion addressingVersion;
    private final @NotNull SOAPVersion soapVersion;
    /**
     * We can't store {@link Message} here as those may get replaced as
     * the packet travels through the pipeline.
     */
    private final @NotNull Packet packet;


    WsaPropertyBag(AddressingVersion addressingVersion, SOAPVersion soapVersion, Packet packet) {
        this.addressingVersion = addressingVersion;
        this.soapVersion = soapVersion;
        this.packet = packet;
    }

    /**
     * Gets the <tt>wsa:To</tt> header.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    @Property(JAXWSProperties.ADDRESSING_TO)
    public String getTo() throws XMLStreamException {
        Header h = packet.getMessage().getHeaders().get(addressingVersion.toTag, false);
        if (h == null) return null;
        return h.getStringContent();
    }

    /**
     * Gets the <tt>wsa:From</tt> header.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    @Property(JAXWSProperties.ADDRESSING_FROM)
    public WSEndpointReference getFrom() throws XMLStreamException {
        return getEPR(addressingVersion.fromTag);
    }

    /**
     * Gets the <tt>wsa:Action</tt> header content as String.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    @Property(JAXWSProperties.ADDRESSING_ACTION)
    public String getAction() {
        Header h = packet.getMessage().getHeaders().get(addressingVersion.actionTag, false);
        if(h==null) return null;
        return h.getStringContent();
    }

    /**
     * Gets the <tt>wsa:MessageID</tt> header content as String.
     *
     * @return
     *      null if the incoming SOAP message didn't have the header.
     */
    // WsaServerTube.REQUEST_MESSAGE_ID is exposed for backward compatibility with 2.1
    @Property({JAXWSProperties.ADDRESSING_MESSAGEID,WsaServerTube.REQUEST_MESSAGE_ID})
    public String getMessageID() {
        return packet.getMessage().getHeaders().getMessageID(addressingVersion,soapVersion);
    }

    private WSEndpointReference getEPR(QName tag) throws XMLStreamException {
        Header h = packet.getMessage().getHeaders().get(tag, false);
        if(h==null) return null;
        return h.readAsEPR(addressingVersion);
    }

    protected PropertyMap getPropertyMap() {
        return model;
    }

    private static final PropertyMap model;
    static {
        model = parse(WsaPropertyBag.class);
    }
}
