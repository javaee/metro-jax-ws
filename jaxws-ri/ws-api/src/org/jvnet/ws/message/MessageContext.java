package org.jvnet.ws.message;

import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

/**
 * MessageContext represents a container of a SOAP message and all the properties
 * including the transport headers.
 *
 * MessageContext is a {@link PropertySet} that combines properties exposed from multiple
 * {@link PropertySet}s into one.
 *
 * <p>
 * This implementation allows one {@link PropertySet} to assemble
 * all properties exposed from other "satellite" {@link PropertySet}s.
 * (A satellite may itself be a {@link DistributedPropertySet}, so
 * in general this can form a tree.)
 */
public interface MessageContext extends PropertySet {
	
	/**
	 * Gets the SAAJ SOAPMessage representation of the SOAP message.
	 * 
	 * @return The SOAPMessage
	 */
	SOAPMessage getSOAPMessage();

	/**
	 * Sets the SAAJ SOAPMessage to be the SOAP message.
	 * 
	 * @param message The SOAP message to set
	 */
	void setSOAPMessage(SOAPMessage message);

    void addSatellite(PropertySet satellite);

    void removeSatellite(PropertySet satellite);

    void copySatelliteInto(MessageContext r);

    <T extends PropertySet> T getSatellite(Class<T> satelliteClass);

	void addTransportHeader(String name, List<String> values);
	
	void addAttachment(String name, DataHandler dh);
    
//    public interface Builder {
//    	MessageContext build();
//    	Builder message(InputStream is);
//    	Builder message(Source s);
//    	Builder message(SOAPMessage m);
//    	Builder transportHeader(String name, List<String> values);
//    	Builder attachment(String name, DataHandler dh);
//    }
}
