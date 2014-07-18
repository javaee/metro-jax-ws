package com.sun.xml.ws.db.xmlbeans;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.WebServiceException;

import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import com.sun.xml.ws.db.xmlbeans.FaultInfo.PropInfo;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;

public class FaultBridge<T> implements XMLBridge<T> {
    private static final Logger logger = Logger.getLogger(FaultBridge.class.getName());
    static final String WrapperPrefix  = "fw";
    static final String WrapperPrefixColon = WrapperPrefix + ":";

    private XMLBeansContext parent;
    private FaultInfo faultInfo;

    private TypeInfo typeInfo;
    private QName xmlTag = null;
    private Class<T> javaType = null;
    private XmlOptions xmlOptions;

    public FaultBridge(XMLBeansContext parent, FaultInfo info) {
        this.parent = parent;
        this.typeInfo = info.implModel.getDetailType();
        this.javaType = (Class<T>) typeInfo.type;
        this.xmlTag = typeInfo.tagName;
        faultInfo = info;
        if (faultInfo.detailInfo != null) {
            if (faultInfo.detailInfo.bridge == null) faultInfo.detailInfo.bridge = parent.bridge(faultInfo.detailInfo.typeInfo);            
        } else {
            for (PropInfo prop : faultInfo.propInfo) if (prop.bridge == null) prop.bridge = parent.bridge(prop.typeInfo); 
        }
    }

    public QName getXmlTag() {
        return xmlTag;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public BindingContext context() {
        return parent;
    }
    
    @Override
    public void marshal(T object, XMLStreamWriter output, AttachmentMarshaller am) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshal(T object, Node output) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshal(T object, Result result) throws JAXBException {
        SAX2DOMContentHandler contentHandler = new SAX2DOMContentHandler();
        if (faultInfo.detailInfo != null) {
            try {
                faultInfo.detailInfo.bridge.marshal(faultInfo.detailInfo.getter.invoke(object, new Object[0]), contentHandler, null);
            } catch (Exception e) {
                throw new WebServiceException(e);
            }    
        } else {
            //TODO reuse JavaBeanBridge
            Attributes att = XMLUtil.emptyAttributes();
            try {
                contentHandler.startPrefixMapping(WrapperPrefix, typeInfo.tagName.getNamespaceURI());
                contentHandler.startElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), WrapperPrefixColon + typeInfo.tagName.getLocalPart(), att);

                if (object == null) {
                    //TODO nil=1?
                } else {
                    for(FaultInfo.PropInfo prop : faultInfo.propInfo) {
                        prop.bridge.marshal(prop.getter.invoke(object), contentHandler, null);
                    }
                }  
                
                contentHandler.endElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), null);
                contentHandler.endPrefixMapping(WrapperPrefix);
            } catch (Exception e) {
                throw new JAXBException(e);
            }
        }
        ((DOMResult)result).setNode(contentHandler.getDocument());
    }

    @Override
    public T unmarshal(XMLStreamReader reader, AttachmentUnmarshaller au) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }
    
    @Override
    public T unmarshal(Source in, AttachmentUnmarshaller au) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public T unmarshal(InputStream in) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public T unmarshal(Node n, AttachmentUnmarshaller au) throws JAXBException {        
        try {
            //TODO use non-default constructor?
            Object object = faultInfo.implModel.getExceptionClass().newInstance();
            //TODO unmarshal and set properties
            return (T)object;
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    @Override
    public boolean supportOutputStream() {
        return false;
    }
}
