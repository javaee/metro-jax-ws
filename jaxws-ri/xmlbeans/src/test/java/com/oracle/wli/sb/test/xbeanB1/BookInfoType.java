/*
 * XML Type:  BookInfoType
 * Namespace: http://www.oracle.com/wli/sb/test/xbean_b1
 * Java type: com.oracle.wli.sb.test.xbeanB1.BookInfoType
 *
 * Automatically generated - do not modify.
 */
package com.oracle.wli.sb.test.xbeanB1;


/**
 * An XML BookInfoType(@http://www.oracle.com/wli/sb/test/xbean_b1).
 *
 * This is a complex type.
 */
public interface BookInfoType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(BookInfoType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s4E34E7B511E3213756E208C14FB9EC07").resolveHandle("bookinfotype9399type");
    
    /**
     * Gets the "name" element
     */
    java.lang.String getName();
    
    /**
     * Gets (as xml) the "name" element
     */
    org.apache.xmlbeans.XmlString xgetName();
    
    /**
     * Sets the "name" element
     */
    void setName(java.lang.String name);
    
    /**
     * Sets (as xml) the "name" element
     */
    void xsetName(org.apache.xmlbeans.XmlString name);
    
    /**
     * Gets the "isbn-10" element
     */
    java.lang.String getIsbn10();
    
    /**
     * Gets (as xml) the "isbn-10" element
     */
    com.oracle.wli.sb.test.xbeanB1.ISBN10 xgetIsbn10();
    
    /**
     * Sets the "isbn-10" element
     */
    void setIsbn10(java.lang.String isbn10);
    
    /**
     * Sets (as xml) the "isbn-10" element
     */
    void xsetIsbn10(com.oracle.wli.sb.test.xbeanB1.ISBN10 isbn10);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType newInstance() {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static com.oracle.wli.sb.test.xbeanB1.BookInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.oracle.wli.sb.test.xbeanB1.BookInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
