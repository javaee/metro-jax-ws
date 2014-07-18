/*
 * An XML document type.
 * Localname: Books
 * Namespace: http://www.oracle.com/wli/sb/test/xbean_b0
 * Java type: com.oracle.wli.sb.test.xbeanB0.BooksDocument
 *
 * Automatically generated - do not modify.
 */
package com.oracle.wli.sb.test.xbeanB0;


/**
 * A document containing one Books(@http://www.oracle.com/wli/sb/test/xbean_b0) element.
 *
 * This is a complex type.
 */
public interface BooksDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(BooksDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s4E34E7B511E3213756E208C14FB9EC07").resolveHandle("books1da1doctype");
    
    /**
     * Gets the "Books" element
     */
    com.oracle.wli.sb.test.xbeanB0.BooksDocument.Books getBooks();
    
    /**
     * Sets the "Books" element
     */
    void setBooks(com.oracle.wli.sb.test.xbeanB0.BooksDocument.Books books);
    
    /**
     * Appends and returns a new empty "Books" element
     */
    com.oracle.wli.sb.test.xbeanB0.BooksDocument.Books addNewBooks();
    
    /**
     * An XML Books(@http://www.oracle.com/wli/sb/test/xbean_b0).
     *
     * This is a complex type.
     */
    public interface Books extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Books.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s4E34E7B511E3213756E208C14FB9EC07").resolveHandle("booksacebelemtype");
        
        /**
         * Gets array of all "book" elements
         */
        com.oracle.wli.sb.test.xbeanB1.BookInfoType[] getBookArray();
        
        /**
         * Gets ith "book" element
         */
        com.oracle.wli.sb.test.xbeanB1.BookInfoType getBookArray(int i);
        
        /**
         * Returns number of "book" element
         */
        int sizeOfBookArray();
        
        /**
         * Sets array of all "book" element
         */
        void setBookArray(com.oracle.wli.sb.test.xbeanB1.BookInfoType[] bookArray);
        
        /**
         * Sets ith "book" element
         */
        void setBookArray(int i, com.oracle.wli.sb.test.xbeanB1.BookInfoType book);
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "book" element
         */
        com.oracle.wli.sb.test.xbeanB1.BookInfoType insertNewBook(int i);
        
        /**
         * Appends and returns a new empty value (as xml) as the last "book" element
         */
        com.oracle.wli.sb.test.xbeanB1.BookInfoType addNewBook();
        
        /**
         * Removes the ith "book" element
         */
        void removeBook(int i);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static com.oracle.wli.sb.test.xbeanB0.BooksDocument.Books newInstance() {
              return (com.oracle.wli.sb.test.xbeanB0.BooksDocument.Books) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static com.oracle.wli.sb.test.xbeanB0.BooksDocument.Books newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (com.oracle.wli.sb.test.xbeanB0.BooksDocument.Books) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument newInstance() {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static com.oracle.wli.sb.test.xbeanB0.BooksDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (com.oracle.wli.sb.test.xbeanB0.BooksDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
