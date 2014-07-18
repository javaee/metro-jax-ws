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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.w3c.dom.Node;

//decompiled from test/resources/databinding.jaxrpc.jar for testComile 
public abstract interface CountryInfoType extends XmlObject
{
  public static final SchemaType type = (SchemaType)XmlBeans.typeSystemForClassLoader(($1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountryInfoType == null ? ($1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountryInfoType = $1.class$("com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType")) : $1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountryInfoType).getClassLoader(), "schemaorg_apache_xmlbeans.system.s09FCA1C7FACD165EE6679C9974C42073").resolveHandle("countryinfotype03f9type");
  static class $1
  {
      static Class class$com$bea$wli$sb$transports$ejb$test$xbean$CountryInfoType;

      static Class class$(String x0)
      {
        try
        {
          return Class.forName(x0); } catch (ClassNotFoundException x1) {  
//        throw new NoClassDefFoundError().initCause(x1);
        throw new RuntimeException(x1);
          }
      }
    }
  public abstract String getCode();

  public abstract Code xgetCode();

  public abstract void setCode(String paramString);

  public abstract void xsetCode(Code paramCode);

  public abstract String getName();

  public abstract XmlString xgetName();

  public abstract void setName(String paramString);

  public abstract void xsetName(XmlString paramXmlString);

  public static final class Factory
  {
    public static CountryInfoType newInstance()
    {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().newInstance(CountryInfoType.type, null);
    }
    public static CountryInfoType newInstance(XmlOptions options) {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().newInstance(CountryInfoType.type, options);
    }

    public static CountryInfoType parse(String xmlAsString) throws XmlException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(xmlAsString, CountryInfoType.type, null);
    }
    public static CountryInfoType parse(String xmlAsString, XmlOptions options) throws XmlException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(xmlAsString, CountryInfoType.type, options);
    }

    public static CountryInfoType parse(File file) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(file, CountryInfoType.type, null);
    }
    public static CountryInfoType parse(File file, XmlOptions options) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(file, CountryInfoType.type, options);
    }
    public static CountryInfoType parse(URL u) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(u, CountryInfoType.type, null);
    }
    public static CountryInfoType parse(URL u, XmlOptions options) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(u, CountryInfoType.type, options);
    }
    public static CountryInfoType parse(InputStream is) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(is, CountryInfoType.type, null);
    }
    public static CountryInfoType parse(InputStream is, XmlOptions options) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(is, CountryInfoType.type, options);
    }
    public static CountryInfoType parse(Reader r) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(r, CountryInfoType.type, null);
    }
    public static CountryInfoType parse(Reader r, XmlOptions options) throws XmlException, IOException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(r, CountryInfoType.type, options);
    }
    public static CountryInfoType parse(XMLStreamReader sr) throws XmlException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(sr, CountryInfoType.type, null);
    }
    public static CountryInfoType parse(XMLStreamReader sr, XmlOptions options) throws XmlException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(sr, CountryInfoType.type, options);
    }
    public static CountryInfoType parse(Node node) throws XmlException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(node, CountryInfoType.type, null);
    }
    public static CountryInfoType parse(Node node, XmlOptions options) throws XmlException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(node, CountryInfoType.type, options);
    }
    /** @deprecated */
    public static CountryInfoType parse(XMLInputStream xis) throws XmlException, XMLStreamException { return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(xis, CountryInfoType.type, null); } 
    /** @deprecated */
    public static CountryInfoType parse(XMLInputStream xis, XmlOptions options) throws XmlException, XMLStreamException {
      return (CountryInfoType)XmlBeans.getContextTypeLoader().parse(xis, CountryInfoType.type, options);
    }
    /** @deprecated */
    public static XMLInputStream newValidatingXMLInputStream(XMLInputStream xis) throws XmlException, XMLStreamException { return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream(xis, CountryInfoType.type, null); } 
    /** @deprecated */
    public static XMLInputStream newValidatingXMLInputStream(XMLInputStream xis, XmlOptions options) throws XmlException, XMLStreamException {
      return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream(xis, CountryInfoType.type, options);
    }
  }
}