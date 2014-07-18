/*
 * An XML document type.
 * Localname: Countries
 * Namespace: http://www.oracle.com/wli/sb/test/xbean_a0
 * Java type: com.oracle.wli.sb.test.xbeanA0.CountriesDocument
 *
 * Automatically generated - do not modify.
 */
package com.oracle.wli.sb.test.xbeanA0;


/**
 * A document containing one Countries(@http://www.oracle.com/wli/sb/test/xbean_a0) element.
 *
 * This is a complex type.
 */
public interface CountriesDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(CountriesDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD3271CF852C571B6E8E7D3BF4E4F3D5A").resolveHandle("countriesceb8doctype");
    
    /**
     * Gets the "Countries" element
     */
    com.oracle.wli.sb.test.xbeanA0.CountriesDocument.Countries getCountries();
    
    /**
     * Sets the "Countries" element
     */
    void setCountries(com.oracle.wli.sb.test.xbeanA0.CountriesDocument.Countries countries);
    
    /**
     * Appends and returns a new empty "Countries" element
     */
    com.oracle.wli.sb.test.xbeanA0.CountriesDocument.Countries addNewCountries();
    
    /**
     * An XML Countries(@http://www.oracle.com/wli/sb/test/xbean_a0).
     *
     * This is a complex type.
     */
    public interface Countries extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Countries.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD3271CF852C571B6E8E7D3BF4E4F3D5A").resolveHandle("countries51b8elemtype");
        
        /**
         * Gets array of all "country" elements
         */
        com.oracle.wli.sb.test.xbeanA1.CountryInfoType[] getCountryArray();
        
        /**
         * Gets ith "country" element
         */
        com.oracle.wli.sb.test.xbeanA1.CountryInfoType getCountryArray(int i);
        
        /**
         * Returns number of "country" element
         */
        int sizeOfCountryArray();
        
        /**
         * Sets array of all "country" element
         */
        void setCountryArray(com.oracle.wli.sb.test.xbeanA1.CountryInfoType[] countryArray);
        
        /**
         * Sets ith "country" element
         */
        void setCountryArray(int i, com.oracle.wli.sb.test.xbeanA1.CountryInfoType country);
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "country" element
         */
        com.oracle.wli.sb.test.xbeanA1.CountryInfoType insertNewCountry(int i);
        
        /**
         * Appends and returns a new empty value (as xml) as the last "country" element
         */
        com.oracle.wli.sb.test.xbeanA1.CountryInfoType addNewCountry();
        
        /**
         * Removes the ith "country" element
         */
        void removeCountry(int i);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument.Countries newInstance() {
              return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument.Countries) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument.Countries newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument.Countries) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument newInstance() {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static com.oracle.wli.sb.test.xbeanA0.CountriesDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.oracle.wli.sb.test.xbeanA0.CountriesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
