/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.util.xml.XmlUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;

/**
 * Implementation of {@link LogicalMessage}. This class implements the methods
 * used by LogicalHandlers to get/set the request or response either
 * as a JAXB object or as javax.xml.transform.Source.
 *
 * <p>The {@link Message} that is passed into the constructor
 * is used to retrieve the payload of the request or response.
 *
 * @see Message
 * @see LogicalMessageContextImpl
 *
 * @author WS Development Team
 */
/**
* TODO: Take care of variations in behavior wrt to vaious sources.
* DOMSource : changes made should be reflected, StreamSource or SAXSource, Give copy
*/
class LogicalMessageImpl implements LogicalMessage {
    private Packet packet;
    // This holds the (modified)payload set by User
    private Source payloadSrc = null;
    // Flag to check if the PayloadSrc is accessed/modified
    private boolean payloadModifed = false;
        
    /** Creates a new instance of LogicalMessageImplRearch */
    public LogicalMessageImpl(Packet packet) {
        // don't create extract payload until Users wants it.
        this.packet = packet;
    }
    
    boolean isPayloadModifed(){
        return payloadModifed;
    }
    Source getModifiedPayload(){
        if(!payloadModifed)
            throw new RuntimeException("Payload not modified.");
        return payloadSrc;
        
    }
    public Source getPayload() {                
        if(!payloadModifed) {
            payloadSrc = packet.getMessage().readPayloadAsSource();
            payloadModifed = true;
        }
        if (payloadSrc == null)
            return null;
        if(payloadSrc instanceof DOMSource){
            return payloadSrc;
        } else {
            try {
            Transformer transformer = XmlUtil.newTransformer();
            DOMResult domResult = new DOMResult();
            transformer.transform(payloadSrc, domResult);
            payloadSrc = new DOMSource(domResult.getNode());
            return payloadSrc;
            } catch(TransformerException te) {
                throw new WebServiceException(te);
            }
        }
        /*
        Source copySrc;
        if(payloadSrc instanceof DOMSource){
            copySrc = payloadSrc;
        } else {
            copySrc = copy(payloadSrc);
        }
        return copySrc;
         */
    }
    
    public void setPayload(Source payload) {
        payloadModifed = true;
        payloadSrc = payload;
    }
    /*
     * Converts to DOMSource and then it unmarshalls this  DOMSource 
     * to a jaxb object. Any changes done in jaxb object are lost if
     * the object isn't set again.
     */
    public Object getPayload(JAXBContext context) {
        try {
            Source payloadSrc = getPayload();
            if(payloadSrc == null)
                return null; 
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller.unmarshal(payloadSrc);
        } catch (JAXBException e){
            throw new WebServiceException(e);
        }
    }
    
    public void setPayload(Object payload, JAXBContext context) {
        payloadModifed = true;
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.fragment", true);
            DOMResult domResult = new DOMResult();
            marshaller.marshal(payload, domResult);
            payloadSrc = new DOMSource(domResult.getNode());
        } catch(JAXBException e) {
            throw new WebServiceException(e);
        }        
    }
    /*
    private Source copy(Source src) {
        if(src instanceof StreamSource){
            StreamSource origSrc = (StreamSource)src;
            byte[] payloadbytes;
            try {
                payloadbytes = ASCIIUtility.getBytes(origSrc.getInputStream());
            } catch (IOException e) {
                throw new WebServiceException(e);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(payloadbytes);
            origSrc.setInputStream(new ByteArrayInputStream(payloadbytes));
            StreamSource copySource = new StreamSource(bis, src.getSystemId());
            return copySource;
        } else if(src instanceof SAXSource){
            SAXSource saxSrc = (SAXSource)src;
            try {
                XMLStreamBuffer xsb = new XMLStreamBuffer();
                XMLReader reader = saxSrc.getXMLReader();
                if(reader == null)
                    reader = new SAXBufferProcessor();
                saxSrc.setXMLReader(reader);
                reader.setContentHandler(new SAXBufferCreator(xsb));
                reader.parse(saxSrc.getInputSource());
                src = new XMLStreamBufferSource(xsb);
                return new XMLStreamBufferSource(xsb);
            } catch (IOException e) {
                throw new WebServiceException(e);
            } catch (SAXException e) {
                throw new WebServiceException(e);
            }
        }
        throw new WebServiceException("Copy is not needed for this Source");
    }
     */
}
