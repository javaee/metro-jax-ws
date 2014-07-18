package com.bea.wli.sb.transports.ejb.test.xbean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.stream.XMLStreamReader;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.w3c.dom.Node;

//decompiled from test/resources/databinding.jaxrpc.jar for testComile 
public abstract interface Code extends XmlString
{
  public static final SchemaType type = (SchemaType)XmlBeans.typeSystemForClassLoader(($1.class$com$bea$wli$sb$transports$ejb$test$xbean$Code == null ? ($1.class$com$bea$wli$sb$transports$ejb$test$xbean$Code = $1.class$("com.bea.wli.sb.transports.ejb.test.xbean.Code")) : $1.class$com$bea$wli$sb$transports$ejb$test$xbean$Code).getClassLoader(), "schemaorg_apache_xmlbeans.system.s09FCA1C7FACD165EE6679C9974C42073").resolveHandle("code9c3atype");
  static class $1 {
      static Class class$com$bea$wli$sb$transports$ejb$test$xbean$Code;
      static Class class$(String x0)
      {
        try
        {
          return Class.forName(x0); } catch (ClassNotFoundException x1) {  
//        throw new NoClassDefFoundError().initCause(x1);
//              return null;
              throw new RuntimeException(x1);
          }
      }
    }
  public static final class Factory
  {
    public static Code newValue(Object obj)
    {
      return (Code)Code.type.newValue(obj);
    }
    public static Code newInstance() {
      return (Code)XmlBeans.getContextTypeLoader().newInstance(Code.type, null);
    }
    public static Code newInstance(XmlOptions options) {
      return (Code)XmlBeans.getContextTypeLoader().newInstance(Code.type, options);
    }

    public static Code parse(String xmlAsString) throws XmlException {
      return (Code)XmlBeans.getContextTypeLoader().parse(xmlAsString, Code.type, null);
    }
    public static Code parse(String xmlAsString, XmlOptions options) throws XmlException {
      return (Code)XmlBeans.getContextTypeLoader().parse(xmlAsString, Code.type, options);
    }

    public static Code parse(File file) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(file, Code.type, null);
    }
    public static Code parse(File file, XmlOptions options) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(file, Code.type, options);
    }
    public static Code parse(URL u) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(u, Code.type, null);
    }
    public static Code parse(URL u, XmlOptions options) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(u, Code.type, options);
    }
    public static Code parse(InputStream is) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(is, Code.type, null);
    }
    public static Code parse(InputStream is, XmlOptions options) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(is, Code.type, options);
    }
    public static Code parse(Reader r) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(r, Code.type, null);
    }
    public static Code parse(Reader r, XmlOptions options) throws XmlException, IOException {
      return (Code)XmlBeans.getContextTypeLoader().parse(r, Code.type, options);
    }
    public static Code parse(XMLStreamReader sr) throws XmlException {
      return (Code)XmlBeans.getContextTypeLoader().parse(sr, Code.type, null);
    }
    public static Code parse(XMLStreamReader sr, XmlOptions options) throws XmlException {
      return (Code)XmlBeans.getContextTypeLoader().parse(sr, Code.type, options);
    }
    public static Code parse(Node node) throws XmlException {
      return (Code)XmlBeans.getContextTypeLoader().parse(node, Code.type, null);
    }
    public static Code parse(Node node, XmlOptions options) throws XmlException {
      return (Code)XmlBeans.getContextTypeLoader().parse(node, Code.type, options);
    }
    /** @deprecated */
    public static Code parse(XMLInputStream xis) throws XmlException, XMLStreamException { return (Code)XmlBeans.getContextTypeLoader().parse(xis, Code.type, null); } 
    /** @deprecated */
    public static Code parse(XMLInputStream xis, XmlOptions options) throws XmlException, XMLStreamException {
      return (Code)XmlBeans.getContextTypeLoader().parse(xis, Code.type, options);
    }
    /** @deprecated */
    public static XMLInputStream newValidatingXMLInputStream(XMLInputStream xis) throws XmlException, XMLStreamException { return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream(xis, Code.type, null); } 
    /** @deprecated */
    public static XMLInputStream newValidatingXMLInputStream(XMLInputStream xis, XmlOptions options) throws XmlException, XMLStreamException {
      return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream(xis, Code.type, options);
    }
  }
}