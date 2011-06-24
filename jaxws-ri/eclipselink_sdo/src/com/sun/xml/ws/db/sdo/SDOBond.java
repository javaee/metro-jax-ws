package com.sun.xml.ws.db.sdo;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;
import org.eclipse.persistence.sdo.SDOConstants;
import org.eclipse.persistence.sdo.helper.SDODataHelper;
import org.eclipse.persistence.sdo.helper.SDOXMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.XMLBridge;

import javax.xml.bind.JAXBException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
//import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 15, 2009
 * Time: 3:44:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class SDOBond<T>  implements XMLBridge<T> {

    private static final String CLASSNAME = SDOBond.class.getName();

    private static final Logger logger = Logger.getLogger( CLASSNAME );

    private TypeInfo ti;
    private QName xmlTag = null;
    private Class<T> javaType = null;
    private Type theType = null;
    private SDOContextWrapper parent;
    
    public SDOBond(SDOContextWrapper parent, TypeInfo ti) {
        this.parent = parent;
        this.ti = ti;
        this.javaType = (Class<T>) ti.type;
        this.xmlTag = ti.tagName;
        HelperContext context = parent.getHelperContext();
        this.theType = context.getTypeHelper().getType(javaType);      
    }
    
    public SDOBond(Class<T> type, QName xml) {
        logger.entering("SDOBond", "constructor");
        javaType = type;
        xmlTag = xml;
        HelperContext context = parent.getHelperContext();
        this.theType = context.getTypeHelper().getType(javaType);      
    }

    public QName getXmlTag() {
        return xmlTag;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    private T deserialize(Source src, javax.xml.bind.attachment.AttachmentUnmarshaller au) {
        try {
            HelperContext context = parent.getHelperContext();
            SDOAttachmentUnmarshaller unmarshaller = null;
            if (au != null)
                unmarshaller = new SDOAttachmentUnmarshaller(au);

            DataFactory dataFactory = context.getDataFactory();
            DataObject loadOptions = dataFactory.create(SDOConstants.ORACLE_SDO_URL, SDOConstants.XMLHELPER_LOAD_OPTIONS);
            //bug 8680450
            loadOptions.set(SDOConstants.TYPE_LOAD_OPTION, theType);
            if (unmarshaller != null)
              loadOptions.set(SDOConstants.ATTACHMENT_UNMARSHALLER_OPTION, unmarshaller);
            XMLDocument xdoc = context.getXMLHelper().load(src, null, loadOptions);
            DataObject obj = xdoc.getRootObject();
            Object o = SDOUtils.unwrapPrimitives(obj);
            return (T) o;  // ClassCast possible without check
        }
        catch (Exception e) {
            throw new SDODatabindingException(e);
        }
    }

    private String serializePrimitive(Object obj, Class<?> contentClass) {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Primitive class to be serialized ==> " + contentClass);    
        HelperContext context = parent.getHelperContext();
        Type type = context.getTypeHelper().getType(contentClass);
        if (type != null)
            return ((SDODataHelper) context.getDataHelper()).convertToStringValue(obj, type);
        
        if (logger.isLoggable(Level.FINE))
        logger.fine("Invalid SDO primitive type: "
                + contentClass.getClass().getName());
        throw new SDODatabindingException("Invalid SDO primitive type: "
                + contentClass.getClass().getName());
    }
    
    private void serializeToResult(String value, Result result) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element elt = doc.createElementNS(xmlTag.getNamespaceURI(), "ns1:"
                    + xmlTag.getLocalPart());
            doc.appendChild(elt);
            elt.appendChild(doc.createTextNode(value));
            DOMSource ds = new DOMSource(elt);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.transform(ds, result);
        } catch (Exception e) {
            throw new SDODatabindingException(e.getMessage());
        }
    }

    //@Override
    public BindingContext context() {

        return parent;
    }
    
    private void serializeDataObject(DataObject java, Result result,
            javax.xml.bind.attachment.AttachmentMarshaller am) {
        logger.entering(CLASSNAME, "serializeDataObject");
        try {
            HelperContext context = parent.getHelperContext();
            SDOAttachmentMarshaller marshaller = null;
            if (am != null)
                marshaller = new SDOAttachmentMarshaller(am);

            // check Primitives for T
            SDOXMLHelper sdoXMLHelper = (SDOXMLHelper) context.getXMLHelper();
            
            // Bug 8909750 - Toplink already sets this to "GMT".  ADF
            // resets it before we get here, so don't change it again.
            //sdoXMLHelper.setTimeZone(TimeZone.getTimeZone("GMT"));
            sdoXMLHelper.setTimeZoneQualified(true);

            XMLDocument xmlDoc = sdoXMLHelper.createDocument((DataObject) java, xmlTag.getNamespaceURI(), xmlTag.getLocalPart());
            if (xmlDoc == null) {
                return;
            }
            xmlDoc.setXMLDeclaration(false);
            DataObject saveOptions = null;
            if (marshaller != null) {
                DataFactory dataFactory = parent.getHelperContext().getDataFactory();
                saveOptions = dataFactory.create(SDOConstants.ORACLE_SDO_URL,
                    SDOConstants.XMLHELPER_LOAD_OPTIONS);
                saveOptions.set(SDOConstants.ATTACHMENT_MARSHALLER_OPTION, marshaller);
            }
            sdoXMLHelper.save(xmlDoc, result, saveOptions);

        } catch (Exception e) {
            throw new SDODatabindingException(e);
        }
    }

    //@Override
    public void marshal(T object, XMLStreamWriter output,
            AttachmentMarshaller am) throws JAXBException {
        /*  Didn't work due to bug 8539542
        StAXResult result = new StAXResult(writer);
        sdoXMLHelper.save(xmlDoc, result, null);
        */
        try {
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SAX2StaxContentHandler handler = new SAX2StaxContentHandler(output);
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, new SAXResult(handler),
                    am);
            return;
        }

       try {
            String value = serializePrimitive(object, javaType);
            String prefix = output.getPrefix(xmlTag.getNamespaceURI());
            //TODO, this is a hack, seems to be wrong. why should namespace returned  is ""?
            if (xmlTag.getNamespaceURI().equals("")) {
                output.writeStartElement("", xmlTag.getLocalPart(), xmlTag.getNamespaceURI());
            } else if (prefix == null) {
                output.writeStartElement(xmlTag.getNamespaceURI(), xmlTag.getLocalPart());
            } else {
                output.writeStartElement(prefix, xmlTag.getLocalPart(), xmlTag.getNamespaceURI());
            }
            output.writeCharacters(value);
            output.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SDODatabindingException(e);
        }
    }

    //@Override
    public void marshal(T object, OutputStream output,
            NamespaceContext nsContext, AttachmentMarshaller am)
            throws JAXBException {
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, new StreamResult(output),
                    am);
            return;
        }
        
        try {
            String value = serializePrimitive(object, javaType);
            String prefix = nsContext.getPrefix(xmlTag.getNamespaceURI());
            StringBuilder sb = new StringBuilder();
            if ("".equals(prefix)) {
                sb.append("<").append(xmlTag.getLocalPart()).append(">");
                sb.append(value);
                sb.append("</").append(xmlTag.getLocalPart()).append(">");
            } else if (prefix != null) {
                sb.append("<").append(prefix).append(":")
                        .append(xmlTag.getLocalPart()).append(">");
                sb.append(value);
                sb.append("</").append(prefix).append(":")
                        .append(xmlTag.getLocalPart()).append(">");
            } else {
                // Unbound namespace!
                prefix = "ns1";
                sb.append("<ns1:").append(xmlTag.getLocalPart())
                        .append(" xmlns:ns1=\"")
                        .append(xmlTag.getNamespaceURI()).append("\"")
                        .append(">");
                sb.append(value);
                sb.append("</ns1:").append(xmlTag.getLocalPart()).append(">");
            }
        } catch (Exception e) {
            throw new SDODatabindingException(e);
        }
    }

    //@Override
    public void marshal(T object, Node output) throws JAXBException {
        Result res = new DOMResult(output);
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, res, null);
            return;
        }
        String value = serializePrimitive(object, javaType);
        serializeToResult(value, res);
    }

    //@Override
    public void marshal(T object, ContentHandler contentHandler,
            AttachmentMarshaller am) throws JAXBException {
        Result res = new SAXResult(contentHandler);
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, res, null);
            return;
        }

        String value = serializePrimitive(object, javaType);
        serializeToResult(value, res);
    }

    //@Override
    public void marshal(T object, Result result) throws JAXBException {
        if (object instanceof DataObject) {
            serializeDataObject((DataObject) object, result, null);
            return;
        }

        String value = serializePrimitive(object, javaType);
        serializeToResult(value, result);
    }

    //@Override
    public T unmarshal(XMLStreamReader in, AttachmentUnmarshaller au)
            throws JAXBException {
        //return deserialize(new StAXSource(in), au);
        return deserialize(new com.sun.xml.ws.util.xml.StAXSource(in, false), au);
    }

    //@Override
    public T unmarshal(Source in, AttachmentUnmarshaller au)
            throws JAXBException {
        return deserialize(in, au);
    }

    //@Override
    public T unmarshal(InputStream in) throws JAXBException {
        return deserialize(new StreamSource(in), null);
    }

    //@Override
    public T unmarshal(Node n, AttachmentUnmarshaller au) throws JAXBException {
        return deserialize(new DOMSource(n), au);
    }

    //@Override
    public TypeInfo getTypeInfo() {
        return ti;
    }

    //@Override
    public boolean supportOutputStream() {
        return true;
    }
    
    private String getExceptionStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
