package com.sun.xml.ws.db.xmlbeans;

import static com.sun.xml.ws.db.xmlbeans.XMLUtil.verifyTag;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.sun.xml.ws.db.xmlbeans.JavaBeanInfo.PropInfo;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;

public class JavaBeanBridge<T> implements XMLBridge<T> {
    private static final Logger logger = Logger.getLogger(JavaBeanBridge.class.getName());
    static final String WrapperPrefix  = "jb";
    static final String WrapperPrefixColon = WrapperPrefix + ":";

    private XMLBeansContext parent;
    private JavaBeanInfo beanInfo;
    private TypeInfo typeInfo;
    private QName xmlTag = null;
    private Class<T> javaType = null;
    private XmlOptions xmlOptions;

    public JavaBeanBridge(XMLBeansContext parent, TypeInfo ti, JavaBeanInfo info) {
        this.parent = parent;
        this.typeInfo = ti;
        this.javaType = (Class<T>) typeInfo.type;
        this.xmlTag = typeInfo.tagName;
        beanInfo = info;
        parent.beanBridges.put(ti, this);
        for(JavaBeanInfo.PropInfo prop : beanInfo.properties) if (prop.bridge == null) prop.bridge = parent.bridge(prop.typeInfo);
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
        try {
            String prefix = output.getPrefix(typeInfo.tagName.getNamespaceURI());
            if (prefix == null) prefix = WrapperPrefix;
            output.writeStartElement(prefix, typeInfo.tagName.getLocalPart(), typeInfo.tagName.getNamespaceURI());
            output.writeNamespace(prefix, typeInfo.tagName.getNamespaceURI());
            if (object == null) {
                //TODO nil=1?
            } else {
                for(JavaBeanInfo.PropInfo prop : beanInfo.properties) {
                    Object val = prop.getter.get(object);
                    if (val != null)prop.bridge.marshal(val, output, am);
                    else {
                        //TODO write nil
                    }
                }
            }  
            output.writeEndElement();          
        } catch (XMLStreamException e) {
            throw new DatabindingException(e);
        }
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
        Attributes att = XMLUtil.emptyAttributes();
        try {
            contentHandler.startPrefixMapping(WrapperPrefix, typeInfo.tagName.getNamespaceURI());
            contentHandler.startElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), WrapperPrefixColon + typeInfo.tagName.getLocalPart(), att);
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
        if (object == null) {
            //TODO nil=1?
        } else {
            for(JavaBeanInfo.PropInfo prop : beanInfo.properties) {
                prop.bridge.marshal(prop.getter.get(object), contentHandler, am);
            }
        }  
        try {
            contentHandler.endElement(typeInfo.tagName.getNamespaceURI(), typeInfo.tagName.getLocalPart(), null);
            contentHandler.endPrefixMapping(WrapperPrefix);
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
    }

    @Override
    public void marshal(T object, Result result) throws JAXBException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public T unmarshal(XMLStreamReader reader, AttachmentUnmarshaller au) throws JAXBException {
        try {
            verifyTag(reader,xmlTag);
            Object obj = beanInfo.contentClass.newInstance();
            reader.nextTag();
            while (reader.getEventType()==XMLStreamReader.START_ELEMENT) {
                QName name = reader.getName();
                JavaBeanInfo.PropInfo prop  = prop(name);
                if(prop != null) {
                    Object propValue = prop.bridge.unmarshal(reader, au);
                    if (propValue != null) prop.setter.set(obj, propValue);
                }
                reader.nextTag();
            }        
            return (T)obj;
        } catch (Exception e) {
            throw new DatabindingException(e);
        }
    }
    
    private PropInfo prop(QName name) {
        for(JavaBeanInfo.PropInfo prop : beanInfo.properties) if (prop.typeInfo.tagName.equals(name)) return prop;
        return null;
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
        //TODO
        throw new UnsupportedOperationException();
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