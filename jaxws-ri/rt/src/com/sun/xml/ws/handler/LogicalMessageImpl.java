/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
    private JAXBContext defaultJaxbContext;
    // This holds the (modified)payload set by User or DOMSource when accessed by User as it can allow
    // direct modification without explicit call to setPayload()
    private Source payloadSrc = null;
    // Flag to check if the PayloadSrc is accessed/modified
    private boolean payloadModifed = false;
        
    /** Creates a new instance of LogicalMessageImplRearch */
    public LogicalMessageImpl(JAXBContext defaultJaxbContext, Packet packet) {
        // don't create extract payload until Users wants it.
        this.packet = packet;
        this.defaultJaxbContext = defaultJaxbContext;
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
            Source payload = packet.getMessage().copy().readPayloadAsSource();
            if(payload instanceof DOMSource) {
                payloadSrc = payload;
                payloadModifed = true;
            } else {
               return payload; 
            }
        }
        return payloadSrc;      
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
        if(context == null) {
            context = defaultJaxbContext;
        }
        if(context == null)
            throw new WebServiceException("JAXBContext parameter cannot be null");
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
        if(context == null) {
            context = defaultJaxbContext;
        }
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
