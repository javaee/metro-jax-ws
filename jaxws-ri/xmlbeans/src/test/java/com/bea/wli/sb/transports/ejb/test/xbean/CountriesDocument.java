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
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.w3c.dom.Node;

//decompiled from test/resources/databinding.jaxrpc.jar for testComile 
public abstract interface CountriesDocument extends XmlObject
{
  public static final SchemaType type = (SchemaType)XmlBeans.typeSystemForClassLoader(($1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument == null ? ($1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument = $1.class$("com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument")) : $1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument).getClassLoader(), "schemaorg_apache_xmlbeans.system.s09FCA1C7FACD165EE6679C9974C42073").resolveHandle("countries721fdoctype");
static class $1
{
    static Class class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument;
    static Class class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument$Countries;

    static Class class$(String x0)
    {
      try
      {
        return Class.forName(x0); } catch (ClassNotFoundException x1) {  
//      throw new NoClassDefFoundError().initCause(x1);
      throw new RuntimeException(x1);}
    }
  }
  public abstract Countries getCountries();

  public abstract void setCountries(Countries paramCountries);

  public abstract Countries addNewCountries();

  public static final class Factory
  {
    public static CountriesDocument newInstance()
    {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().newInstance(CountriesDocument.type, null);
    }
    public static CountriesDocument newInstance(XmlOptions options) {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().newInstance(CountriesDocument.type, options);
    }

    public static CountriesDocument parse(String xmlAsString) throws XmlException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(xmlAsString, CountriesDocument.type, null);
    }
    public static CountriesDocument parse(String xmlAsString, XmlOptions options) throws XmlException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(xmlAsString, CountriesDocument.type, options);
    }

    public static CountriesDocument parse(File file) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(file, CountriesDocument.type, null);
    }
    public static CountriesDocument parse(File file, XmlOptions options) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(file, CountriesDocument.type, options);
    }
    public static CountriesDocument parse(URL u) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(u, CountriesDocument.type, null);
    }
    public static CountriesDocument parse(URL u, XmlOptions options) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(u, CountriesDocument.type, options);
    }
    public static CountriesDocument parse(InputStream is) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(is, CountriesDocument.type, null);
    }
    public static CountriesDocument parse(InputStream is, XmlOptions options) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(is, CountriesDocument.type, options);
    }
    public static CountriesDocument parse(Reader r) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(r, CountriesDocument.type, null);
    }
    public static CountriesDocument parse(Reader r, XmlOptions options) throws XmlException, IOException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(r, CountriesDocument.type, options);
    }
    public static CountriesDocument parse(XMLStreamReader sr) throws XmlException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(sr, CountriesDocument.type, null);
    }
    public static CountriesDocument parse(XMLStreamReader sr, XmlOptions options) throws XmlException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(sr, CountriesDocument.type, options);
    }
    public static CountriesDocument parse(Node node) throws XmlException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(node, CountriesDocument.type, null);
    }
    public static CountriesDocument parse(Node node, XmlOptions options) throws XmlException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(node, CountriesDocument.type, options);
    }
    /** @deprecated */
    public static CountriesDocument parse(XMLInputStream xis) throws XmlException, XMLStreamException { return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(xis, CountriesDocument.type, null); } 
    /** @deprecated */
    public static CountriesDocument parse(XMLInputStream xis, XmlOptions options) throws XmlException, XMLStreamException {
      return (CountriesDocument)XmlBeans.getContextTypeLoader().parse(xis, CountriesDocument.type, options);
    }
    /** @deprecated */
    public static XMLInputStream newValidatingXMLInputStream(XMLInputStream xis) throws XmlException, XMLStreamException { return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream(xis, CountriesDocument.type, null); } 
    /** @deprecated */
    public static XMLInputStream newValidatingXMLInputStream(XMLInputStream xis, XmlOptions options) throws XmlException, XMLStreamException {
      return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream(xis, CountriesDocument.type, options);
    }
  }

  public static abstract interface Countries extends XmlObject
  {
    public static final SchemaType type = (SchemaType)XmlBeans.typeSystemForClassLoader((CountriesDocument.$1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument$Countries == null ? (CountriesDocument.$1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument$Countries = CountriesDocument.$1.class$("com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument$Countries")) : CountriesDocument.$1.class$com$bea$wli$sb$transports$ejb$test$xbean$CountriesDocument$Countries).getClassLoader(), "schemaorg_apache_xmlbeans.system.s09FCA1C7FACD165EE6679C9974C42073").resolveHandle("countries4f1felemtype");

    public abstract CountryInfoType[] getCountryArray();

    public abstract CountryInfoType getCountryArray(int paramInt);

    public abstract int sizeOfCountryArray();

    public abstract void setCountryArray(CountryInfoType[] paramArrayOfCountryInfoType);

    public abstract void setCountryArray(int paramInt, CountryInfoType paramCountryInfoType);

    public abstract CountryInfoType insertNewCountry(int paramInt);

    public abstract CountryInfoType addNewCountry();

    public abstract void removeCountry(int paramInt);

    public static final class Factory
    {
      public static CountriesDocument.Countries newInstance()
      {
        return (CountriesDocument.Countries)XmlBeans.getContextTypeLoader().newInstance(CountriesDocument.Countries.type, null);
      }
      public static CountriesDocument.Countries newInstance(XmlOptions options) {
        return (CountriesDocument.Countries)XmlBeans.getContextTypeLoader().newInstance(CountriesDocument.Countries.type, options);
      }
    }
  }
}